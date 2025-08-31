package com.ringme.cms.controller.gym;

import com.ringme.cms.dto.gym.MemberSubscriptionDto;
import com.ringme.cms.dto.gym.PaymentDto;
import com.ringme.cms.model.gym.MemberSubscription;
import com.ringme.cms.model.gym.Payment;
import com.ringme.cms.repository.gym.MemberSubscriptionRepository;
import com.ringme.cms.repository.gym.PaymentRepository;
import com.ringme.cms.service.gym.MemberSubscriptionService;
import com.ringme.cms.service.gym.PaymentService;
import com.ringme.cms.service.gym.PaypalService;
import com.ringme.cms.service.gym.QrCodeService;
import com.ringme.cms.utils.AppUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Controller
@Log4j2
@RequestMapping("/subscription")
@RequiredArgsConstructor
public class MemberSubscriptionController {

    private final MemberSubscriptionRepository memberSubscriptionRepository;

    private final ModelMapper modelMapper;

    private final MemberSubscriptionService memberSubscriptionService;

    private final PaymentService paymentService;
    private final PaypalService paypalService;
    private final PaymentRepository paymentRepository;
    private final QrCodeService qrCodeService;

    @GetMapping("/update")
    public String update(@RequestParam(value = "id") Long id, ModelMap model) {
        log.info("id: {}", id);
        try {
            MemberSubscriptionDto formDto = (MemberSubscriptionDto) model.getOrDefault("model", new MemberSubscriptionDto());
            MemberSubscription memberSubscription = memberSubscriptionRepository.findById(id).orElseThrow();
            if (!model.containsAttribute("model")) {
                formDto = modelMapper.map(memberSubscription, MemberSubscriptionDto.class);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                formDto.setStartEndString(memberSubscription.getStartAt().format(formatter) + " - "  + memberSubscription.getEndAt().format(formatter));
            }
            model.putIfAbsent("model", formDto);

            return "gym/fragment/member :: editMemberSubscription";
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            return "404";
        }
    }

    @GetMapping("/create")
    public String create(@RequestParam(value = "id") Long memberId, ModelMap model) {
        try {
            MemberSubscriptionDto dto = new  MemberSubscriptionDto();
            dto.setMemberId(memberId);
            model.putIfAbsent("model", dto);

            return "gym/fragment/member :: addMemberSubscription";
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            return "404";
        }
    }

    @GetMapping("/detail")
    public String detail(@RequestParam(value = "id") Long id, ModelMap model) {
        log.info("id: {}", id);
        try {
            MemberSubscription memberSubscription = memberSubscriptionRepository.findById(id).orElseThrow();
            model.putIfAbsent("model", memberSubscription);

            return "gym/fragment/member :: memberSubscriptionDetail";
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            return "404";
        }
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("model") MemberSubscriptionDto formDto,
                       BindingResult bindingResult,
                       RedirectAttributes redirectAttributes,
                       HttpServletRequest request) {
        try {
            if (bindingResult.hasErrors()) {
                return AppUtils.goBackWithError(request, redirectAttributes, "form", bindingResult, formDto, null)
                        .orElse("redirect:/member/detail?id=" + formDto.getId() + "&tab=2");
            }
            MemberSubscription memberSubscription = memberSubscriptionService.save(formDto);
            if (formDto.getId() == null) {
                if(formDto.getPaymentGateway().equals("paypal"))
                {
                    if(formDto.getIsRecurring() == 0)
                    {
                        PaymentDto paymentDto = new PaymentDto();
                        paymentDto.setMemberId(memberSubscription.getMember().getId());
                        paymentDto.setDescription(memberSubscription.getMembership().getName());
                        paymentDto.setAmount(memberSubscription.getMembership().getPrice().longValue());
                        paymentDto.setPaymentGateway("paypal");
                        paymentDto.setType(1);
                        paymentDto.setShouldReload(true);
                        paymentDto.setTab(2);
                        com.ringme.cms.model.gym.Payment curPayment = paymentService.save(paymentDto);
                        com.paypal.api.payments.Payment payment = paypalService.createPayment(
                                Double.parseDouble(paymentDto.getAmount().toString()),
                                "USD",
                                "paypal",
                                "sale",
                                paymentDto.getDescription(),
                                "http://192.168.1.214:8086/nexia-cms/payment/cancel/" + curPayment.getId(),
                                "http://192.168.1.214:8086/nexia-cms/payment/success"
                        );

                        String approvalLink = paypalService.getApprovalLink(payment)
                                .orElseThrow(() -> new RuntimeException("PayPal approval link not found."));
                        curPayment.setGatewayTransactionId(payment.getId());
                        curPayment.setPaymentUrl(approvalLink);
                        paymentRepository.save(curPayment);
                        memberSubscription.setPaypalSubscriptionId(payment.getId());
                        memberSubscriptionRepository.save(memberSubscription);
                        byte[] qrCodeBytes = qrCodeService.generateQRCodeImage(approvalLink, 350, 350);

                        String qrCodeBase64 = Base64.getEncoder().encodeToString(qrCodeBytes);

                        redirectAttributes.addFlashAttribute("qrCodeImage", qrCodeBase64);
                        redirectAttributes.addFlashAttribute("payPalLink", approvalLink);
                        redirectAttributes.addFlashAttribute("paymentId", payment.getId());
                        if(paymentDto.getShouldReload() != null)
                        {
                            redirectAttributes.addFlashAttribute("shouldReload", paymentDto.getShouldReload());
                        }
                        return "redirect:/member/detail?id=" + paymentDto.getMemberId() + "&tab=" + paymentDto.getTab();
                    }
                }
                else {
                    redirectAttributes.addFlashAttribute("success", "Thêm thành công!");
                    return AppUtils.goBack(request).orElse("redirect:/member/detail?id=" + memberSubscription.getMember().getId() + "&tab=2");
                }
            } else {
                redirectAttributes.addFlashAttribute("success", "Cập nhật thành công!");
            }
            return AppUtils.goBack(request).orElse("redirect:/member/detail?id=" + formDto.getId() + "&tab=2");
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            return AppUtils.goBackWithError(request, redirectAttributes, "form", bindingResult, formDto, "Error in server!")
                    .orElse("redirect:/member/detail?id=" + formDto.getId() + "&tab=2");
        }
    }
}
