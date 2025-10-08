package com.ringme.cms.controller.gym;

import com.fasterxml.jackson.databind.JsonNode;
import com.paypal.orders.Order;
import com.ringme.cms.dto.gym.MemberSubscriptionDto;
import com.ringme.cms.dto.gym.PaymentDto;
import com.ringme.cms.dto.gym.RecalculateDto;
import com.ringme.cms.model.gym.MemberSubscription;
import com.ringme.cms.model.gym.Payment;
import com.ringme.cms.repository.gym.MemberRepository;
import com.ringme.cms.repository.gym.MemberSubscriptionRepository;
import com.ringme.cms.repository.gym.PaymentRepository;
import com.ringme.cms.service.gym.*;
import com.ringme.cms.utils.AppUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Controller
@Log4j2
@RequestMapping("/subscription")
@RequiredArgsConstructor
public class MemberSubscriptionController {
    @Value("${ipv4.address}")
    private String ipv4;

    private final MemberSubscriptionRepository memberSubscriptionRepository;

    private final ModelMapper modelMapper;

    private final MemberSubscriptionService memberSubscriptionService;

    private final PaymentService paymentService;
    private final PaypalService paypalService;
    private final PaymentRepository paymentRepository;
    private final QrCodeService qrCodeService;
    private final PaypalSubscriptionService  paypalSubscriptionService;
    private final MemberRepository memberRepository;

    @GetMapping("/update")
    public String update(@RequestParam(value = "id") Long id, ModelMap model) {
        log.info("id: {}", id);
        try {
            MemberSubscriptionDto formDto = (MemberSubscriptionDto) model.getOrDefault("model", new MemberSubscriptionDto());
            MemberSubscription memberSubscription = memberSubscriptionRepository.findById(id).orElseThrow();
            if (!model.containsAttribute("model")) {
                formDto = modelMapper.map(memberSubscription, MemberSubscriptionDto.class);
                formDto.setStatusSubscription(memberSubscription.getStatus());
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                formDto.setStartEndString(memberSubscription.getStartAt().format(formatter) + " - "  + memberSubscription.getEndAt().format(formatter));
            }
            model.putIfAbsent("model", formDto);
            List<MemberSubscription> memberSubscriptionList = memberSubscriptionRepository.findByMemberIdAndType(memberRepository.findIdByMemberSubscriptionId(id), memberSubscription.getMembership().getType());
            memberSubscriptionList.sort(Comparator.comparing(MemberSubscription::getStartAt));
            MemberSubscription lastSubscription = memberSubscriptionList.get(memberSubscriptionList.size() - 1);
            if(lastSubscription.getPaypalSubscriptionId() != null && lastSubscription.getPaypalSubscriptionId().startsWith("I-"))
            {
                model.put("allowResub", true);
            }
            model.put("subscriptionAmount",  memberSubscriptionList.size());
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

    @GetMapping("/recalculate-form")
    public String recalculateForm(@RequestParam(value = "id") Long id, @RequestParam(value = "type") Integer type, ModelMap model) {
        try {
            RecalculateDto dto = new  RecalculateDto();
            dto.setId(id);
            dto.setType(type);
            model.putIfAbsent("model", dto);

            return "gym/fragment/member :: recalculateDate";
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            return "404";
        }
    }

    @PostMapping("/recalculate")
    public String recalculate(@ModelAttribute("model") RecalculateDto formDto,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes,
                              HttpServletRequest request) {
        try {
            memberSubscriptionService.recalculate(formDto);
            redirectAttributes.addFlashAttribute("success", "Tính toán lại ngày thành công!");
            return AppUtils.goBack(request).orElse("redirect:/member/index");
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            return AppUtils.goBackWithError(request, redirectAttributes, "form", bindingResult, formDto, "Error in server!")
                    .orElse("redirect:/member/index");
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
                        paymentDto.setAmount(memberSubscription.getMembership().getPrice());
                        paymentDto.setPaymentGateway("paypal");
                        paymentDto.setType(1);
                        paymentDto.setShouldReload(true);
                        paymentDto.setTab(2);
                        Payment curPayment = paymentService.save(paymentDto);
                        Order order = paypalService.createOrder(
                                Double.parseDouble(paymentDto.getAmount().toString()),
                                "USD",
                                paymentDto.getDescription(),
                                "http://" + ipv4  + ":8086/nexia-cms/payment/cancel/" + curPayment.getId(),
                                "http://" + ipv4  + ":8086/nexia-cms/payment/success"
                        );

                        String approvalLink = paypalService.getApprovalLink(order)
                                .orElseThrow(() -> new RuntimeException("PayPal approval link not found in V2 order."));
                        String orderId = order.id();

                        curPayment.setGatewayTransactionId(orderId);
                        curPayment.setPaymentUrl(approvalLink);
                        paymentRepository.save(curPayment);
                        memberSubscription.setPaypalSubscriptionId(orderId);
                        memberSubscriptionRepository.save(memberSubscription);
                        byte[] qrCodeBytes = qrCodeService.generateQRCodeImage(approvalLink, 350, 350);
                        String qrCodeBase64 = Base64.getEncoder().encodeToString(qrCodeBytes);

                        redirectAttributes.addFlashAttribute("qrCodeImage", qrCodeBase64);
                        redirectAttributes.addFlashAttribute("payPalLink", approvalLink);
                        redirectAttributes.addFlashAttribute("paymentId", orderId);
                        if(paymentDto.getShouldReload() != null)
                        {
                            redirectAttributes.addFlashAttribute("shouldReload", paymentDto.getShouldReload());
                        }
                        return "redirect:/member/detail?id=" + paymentDto.getMemberId() + "&tab=" + paymentDto.getTab();
                    }
                    else if(formDto.getIsRecurring() == 1){
                        String baseUrl = "http://" + ipv4  + ":8086/nexia-cms"; // Your base URL
                        String returnUrl = baseUrl + "/subscription/success";
                        String approvalLink = "";
                        String planId = memberSubscription.getMembership().getPaypalPlanId();
                        PaymentDto paymentDto = new PaymentDto();
                        paymentDto.setMemberId(memberSubscription.getMember().getId());
                        paymentDto.setDescription(memberSubscription.getMembership().getName());
                        paymentDto.setAmount(memberSubscription.getMembership().getPrice());
                        paymentDto.setPaymentGateway("paypal");
                        paymentDto.setType(1);
                        paymentDto.setShouldReload(true);
                        paymentDto.setTab(2);
                        Payment curPayment = paymentService.save(paymentDto);
                        String cancelUrl = baseUrl + "/subscription/cancel/" + curPayment.getId();
                        if(planId != null && !planId.trim().isEmpty())
                        {
                            JsonNode subscriptionResponse = paypalSubscriptionService.createSubscription(planId, returnUrl, cancelUrl);
                            String subscriptionId = subscriptionResponse.path("id").asText();
                            for (JsonNode linkNode : subscriptionResponse.path("links")) {
                                if ("approve".equalsIgnoreCase(linkNode.path("rel").asText())) {
                                    approvalLink = linkNode.path("href").asText();
                                    break;
                                }
                            }
                            curPayment.setGatewayTransactionId(subscriptionId);
                            curPayment.setPaymentUrl(approvalLink);
                            paymentRepository.save(curPayment);
                            memberSubscription.setPaypalSubscriptionId(subscriptionId);
                            memberSubscriptionRepository.save(memberSubscription);
                            byte[] qrCodeBytes = qrCodeService.generateQRCodeImage(approvalLink, 350, 350);
                            String qrCodeBase64 = Base64.getEncoder().encodeToString(qrCodeBytes);
                            redirectAttributes.addFlashAttribute("qrCodeImage", qrCodeBase64);
                            redirectAttributes.addFlashAttribute("payPalLink", approvalLink);
                            redirectAttributes.addFlashAttribute("paymentId", subscriptionId);
                            if(paymentDto.getShouldReload() != null)
                            {
                                redirectAttributes.addFlashAttribute("shouldReload", paymentDto.getShouldReload());
                            }
                            return "redirect:/member/detail?id=" + paymentDto.getMemberId() + "&tab=" + paymentDto.getTab();
                        }
                        else {
                            curPayment.setStatus(-1);
                            paymentRepository.save(curPayment);
                            memberSubscription.setStatus(-1);
                            memberSubscriptionRepository.save(memberSubscription);
                            throw new Exception();
                        }
                    }
                }
                else {
                    PaymentDto paymentDto = new PaymentDto();
                    paymentDto.setMemberId(memberSubscription.getMember().getId());
                    paymentDto.setDescription(memberSubscription.getMembership().getName());
                    paymentDto.setAmount(memberSubscription.getMembership().getPrice());
                    paymentDto.setPaymentGateway("cash");
                    paymentDto.setType(1);
                    Payment curPayment = paymentService.save(paymentDto);
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

    @PostMapping(value = {"/delete/{id}"})
    public String delete(@PathVariable(required = true) Long id,
                         HttpServletRequest request,
                         RedirectAttributes redirectAttributes) {
        log.info("id: {}", id);
        try {
            memberSubscriptionService.softDelete(id);
            redirectAttributes.addFlashAttribute("success", "Hủy thành công!");
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error in server!");
        }
        return AppUtils.goBack(request).orElse("redirect:/member/index");
    }

    @PostMapping(value = {"/resub/{id}"})
    public String resub(@PathVariable Long id, @RequestParam(name = "paymentGateway") String paymentGateway,
                        HttpServletRequest request, RedirectAttributes redirectAttributes)
    {
        Long memberId = 0L;
        try
        {
            MemberSubscription curMemberSub = memberSubscriptionRepository.findById(id).orElseThrow();
            if(paymentGateway.equalsIgnoreCase("paypal"))
            {
                memberId = memberSubscriptionService.resub(id, 2);
                PaymentDto paymentDto = new PaymentDto();
                paymentDto.setMemberId(curMemberSub.getMember().getId());
                paymentDto.setDescription(curMemberSub.getMembership().getName());
                paymentDto.setAmount(curMemberSub.getMembership().getPrice());
                paymentDto.setPaymentGateway("paypal");
                paymentDto.setType(1);
                paymentDto.setShouldReload(true);
                paymentDto.setTab(2);
                Payment curPayment = paymentService.save(paymentDto);
                Order order = paypalService.createOrder(
                        Double.parseDouble(paymentDto.getAmount().toString()),
                        "USD",
                        paymentDto.getDescription(),
                        "http://" + ipv4  + ":8086/nexia-cms/payment/cancel/" + curPayment.getId(),
                        "http://" + ipv4  + ":8086/nexia-cms/payment/success"
                );

                String approvalLink = paypalService.getApprovalLink(order)
                        .orElseThrow(() -> new RuntimeException("PayPal approval link not found in V2 order."));
                String orderId = order.id();

                curPayment.setGatewayTransactionId(orderId);
                curPayment.setPaymentUrl(approvalLink);
                paymentRepository.save(curPayment);
                curMemberSub.setPaypalSubscriptionId(orderId);
                memberSubscriptionRepository.save(curMemberSub);
                byte[] qrCodeBytes = qrCodeService.generateQRCodeImage(approvalLink, 350, 350);
                String qrCodeBase64 = Base64.getEncoder().encodeToString(qrCodeBytes);

                redirectAttributes.addFlashAttribute("qrCodeImage", qrCodeBase64);
                redirectAttributes.addFlashAttribute("payPalLink", approvalLink);
                redirectAttributes.addFlashAttribute("paymentId", orderId);
                if(paymentDto.getShouldReload() != null)
                {
                    redirectAttributes.addFlashAttribute("shouldReload", paymentDto.getShouldReload());
                }
                return "redirect:/member/detail?id=" + paymentDto.getMemberId() + "&tab=" + paymentDto.getTab();
            }
            else if(paymentGateway.equalsIgnoreCase("cash"))
            {
                memberId = memberSubscriptionService.resub(id, 1);
                PaymentDto paymentDto = new PaymentDto();
                paymentDto.setMemberId(memberId);
                paymentDto.setDescription(curMemberSub.getMembership().getName());
                paymentDto.setAmount(curMemberSub.getMembership().getPrice());
                paymentDto.setPaymentGateway("cash");
                paymentDto.setType(1);
                Payment curPayment = paymentService.save(paymentDto);
                redirectAttributes.addFlashAttribute("success", "Đăng ký lại thành công");
                return AppUtils.goBack(request).orElse("redirect:/member/detail?id=" + memberId + "&tab=2");
            }
            else throw new IllegalArgumentException();
        }
        catch (Exception e)
        {
            log.error("Exception: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Đăng ký lại thất bại");
            return AppUtils.goBack(request).orElse("redirect:/member/detail?id=" + memberId + "&tab=2");
        }
    }
}
