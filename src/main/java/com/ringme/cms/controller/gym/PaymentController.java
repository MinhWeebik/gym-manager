package com.ringme.cms.controller.gym;

import com.paypal.orders.Order;
import com.ringme.cms.dto.gym.MemberSubscriptionDto;
import com.ringme.cms.dto.gym.PaymentDto;
import com.ringme.cms.model.gym.Member;
import com.ringme.cms.model.gym.MemberSubscription;
import com.ringme.cms.model.gym.Payment;
import com.ringme.cms.repository.gym.MemberRepository;
import com.ringme.cms.repository.gym.MemberSubscriptionRepository;
import com.ringme.cms.repository.gym.PaymentRepository;
import com.ringme.cms.service.gym.MemberSubscriptionService;
import com.ringme.cms.service.gym.PaymentService;
import com.ringme.cms.service.gym.PaypalService;
import com.ringme.cms.service.gym.QrCodeService;
import com.ringme.cms.utils.AppUtils;
import javassist.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Map;

@Controller
@Log4j2
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final MemberRepository memberRepository;
    @Value("${ipv4.address}")
    private String ipv4;

    private final ModelMapper modelMapper;

    private final PaymentService paymentService;

    private final PaypalService paypalService;

    private final QrCodeService qrCodeService;

    private final PaymentRepository paymentRepository;

    private final MemberSubscriptionRepository memberSubscriptionRepository;
    @GetMapping("/create")
    public String update(@RequestParam(value = "id") Long memberId, ModelMap model) {
        log.info("member id: {}", memberId);
        try {
            PaymentDto formDto = new PaymentDto();
            formDto.setMemberId(memberId);
            model.put("model", formDto);

            return "gym/fragment/member :: addPayment";
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            return "404";
        }
    }

    @PostMapping("/save-cash")
    public String saveCash(@Valid @ModelAttribute("model") PaymentDto formDto,
                       BindingResult bindingResult,
                       RedirectAttributes redirectAttributes,
                       HttpServletRequest request) {
        try {
            if (bindingResult.hasErrors()) {
                return AppUtils.goBackWithError(request, redirectAttributes, "form", bindingResult, formDto, null)
                        .orElse("redirect:/member/detail?id=" + formDto.getMemberId() + "&tab=2");
            }
            paymentService.save(formDto);
            if (formDto.getId() == null) {
                redirectAttributes.addFlashAttribute("success", "Thanh toán thành công!");
                return "redirect:/member/detail?id=" + formDto.getMemberId() + "&tab=2";
            } else {
                redirectAttributes.addFlashAttribute("success", "Cập nhật thành công!");
            }
            return AppUtils.goBack(request).orElse("redirect:/member/detail?id=" + formDto.getMemberId() + "&tab=2");
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            return AppUtils.goBackWithError(request, redirectAttributes, "form", bindingResult, formDto, "Error in server!")
                    .orElse("redirect:/member/detail?id=" + formDto.getMemberId() + "&tab=2");
        }
    }

    @PostMapping("/save-paypal")
    public String savePaypal(@Valid @ModelAttribute("model") PaymentDto formDto,
                       BindingResult bindingResult,
                       RedirectAttributes redirectAttributes,
                       HttpServletRequest request) {
        try {
            if(formDto.getTab() == null) formDto.setTab(1);
            if (bindingResult.hasErrors()) {
                return AppUtils.goBackWithError(request, redirectAttributes, "form", bindingResult, formDto, null)
                        .orElse("redirect:/member/detail?id=" + formDto.getMemberId() + "&tab=" + formDto.getTab());
            }
            com.ringme.cms.model.gym.Payment curPayment = paymentService.save(formDto);
            Order order = paypalService.createOrder(
                    Double.parseDouble(formDto.getAmount().toString()),
                    "USD",
                    formDto.getDescription(),
                    "http://" + ipv4  + ":8086/nexia-cms/payment/cancel/" + curPayment.getId(),
                    "http://" + ipv4  + ":8086/nexia-cms/payment/success"
            );

            String approvalLink = paypalService.getApprovalLink(order)
                    .orElseThrow(() -> new RuntimeException("PayPal approval link not found in V2 order."));
            String orderId = order.id();

            curPayment.setGatewayTransactionId(orderId);
            curPayment.setPaymentUrl(approvalLink);
            paymentRepository.save(curPayment);
            byte[] qrCodeBytes = qrCodeService.generateQRCodeImage(approvalLink, 350, 350);
            String qrCodeBase64 = Base64.getEncoder().encodeToString(qrCodeBytes);

            redirectAttributes.addFlashAttribute("qrCodeImage", qrCodeBase64);
            redirectAttributes.addFlashAttribute("payPalLink", approvalLink);
            redirectAttributes.addFlashAttribute("paymentId", orderId);
            if(formDto.getShouldReload() != null)
            {
                redirectAttributes.addFlashAttribute("shouldReload", formDto.getShouldReload());
            }
            return "redirect:/member/detail?id=" + formDto.getMemberId() + "&tab=" + formDto.getTab();
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            return AppUtils.goBackWithError(request, redirectAttributes, "form", bindingResult, formDto, "Error in server!")
                    .orElse("redirect:/member/detail?id=" + formDto.getMemberId() + "&tab=" + formDto.getTab());
        }
    }

    @GetMapping("/success")
    public String paymentSuccess(
            @RequestParam("token") String orderId,
            @RequestParam("PayerID") String payerId
    ) {
        log.info("Executing payment. PaymentId: {}, PayerId: {}", orderId, payerId);

        try {
            Order capturedOrder = paypalService.captureOrder(orderId);

            if ("COMPLETED".equalsIgnoreCase(capturedOrder.status())) {
                log.info("Payment successful for V2 OrderId: {}", capturedOrder.id());
                com.ringme.cms.model.gym.Payment paymentModel = paymentRepository.findByGatewayTransactionId(orderId);
                if (paymentModel != null) {
                    paymentModel.setStatus(1);
                    paymentRepository.save(paymentModel);
                }
                MemberSubscription memberSubscription = memberSubscriptionRepository.findByPaypalSubscriptionId(orderId);
                if (memberSubscription != null && memberSubscription.getStatus() == 2) {
                    memberSubscription.setStatus(1);
                    memberSubscriptionRepository.save(memberSubscription);
                }
                return "gym/payment/success";
            } else {
                com.ringme.cms.model.gym.Payment paymentModel = paymentRepository.findByGatewayTransactionId(orderId);
                if (paymentModel != null) {
                    paymentModel.setStatus(-1);
                    paymentRepository.save(paymentModel);
                }
                MemberSubscription memberSubscription = memberSubscriptionRepository.findByPaypalSubscriptionId(orderId);
                if (memberSubscription != null) {
                    memberSubscription.setStatus(-1);
                    memberSubscriptionRepository.save(memberSubscription);
                }
                log.warn("Payment not completed. Status: {}", capturedOrder.status());
                return "gym/payment/cancel";
            }
        } catch (IOException e) {
            log.error("Error executing payment for PaymentId: " + orderId, e);
            return "gym/payment/cancel";
        }
    }

    @GetMapping("/cancel/{id}")
    public String paymentCancel(
            @PathVariable Long id
    ) {
        log.info("Cancelling payment. PaymentId: {}", id);
        try {
            com.ringme.cms.model.gym.Payment paymentModel = paymentRepository.findById(id).orElse(null);
            if (paymentModel != null) {
                paymentModel.setStatus(-1);
                paymentRepository.save(paymentModel);
                MemberSubscription memberSubscription = memberSubscriptionRepository.findByPaypalSubscriptionId(paymentModel.getGatewayTransactionId());
                if (memberSubscription != null) {
                    memberSubscription.setStatus(-1);
                    memberSubscriptionRepository.save(memberSubscription);
                }
            }
            return "gym/payment/cancel";
        } catch (Exception e) {
            log.error("Error executing payment for PaymentId: " + id, e);
            return "gym/payment/cancel";
        }
    }

    @PostMapping("/regenerate-qr")
    public String regenerateQR(@RequestParam("paymentId") Long paymentId, @RequestParam("memberId") Long memberId, @RequestParam("tab") Integer tab,
                               RedirectAttributes redirectAttributes,
                               HttpServletRequest request)
    {
        try
        {
            Payment payment = paymentRepository.findById(paymentId).orElseThrow();
            if(payment.getPaymentUrl() != null && !payment.getPaymentUrl().isEmpty())
            {
                byte[] qrCodeBytes = qrCodeService.generateQRCodeImage(payment.getPaymentUrl(), 350, 350);
                String qrCodeBase64 = Base64.getEncoder().encodeToString(qrCodeBytes);
                redirectAttributes.addFlashAttribute("qrCodeImage", qrCodeBase64);
                redirectAttributes.addFlashAttribute("paymentId", payment.getGatewayTransactionId());
                redirectAttributes.addFlashAttribute("payPalLink", payment.getPaymentUrl());
                    redirectAttributes.addFlashAttribute("shouldReload", true);
            }
            else throw new Exception("QR code not found");
            return "redirect:/member/detail?id=" + memberId + "&tab=" + tab;
        }
        catch (Exception e)
        {
            redirectAttributes.addFlashAttribute("error", "Sinh mã QR thất bại");
            log.error("Exception: {}", e.getMessage(), e);
            return AppUtils.goBack(request).orElse("redirect:/member/detail?id=" + memberId + "&tab=" + tab);
        }
    }

    @GetMapping("/qr-image")
    public String showJustTheQrCode(@RequestParam("url") String payPalUrl, Model model) {
        try {
            byte[] qrCodeBytes = qrCodeService.generateQRCodeImage(payPalUrl, 400, 400);

            String qrCodeBase64 = Base64.getEncoder().encodeToString(qrCodeBytes);

            model.addAttribute("qrCodeImage", qrCodeBase64);

        } catch (Exception e) {
            model.addAttribute("error", "Could not generate QR code image.");
            log.error("Error: {}", e.getMessage(), e);
        }

        return "gym/payment/qrcode";
    }

    @GetMapping("/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getPaymentStatus(@RequestParam(name = "paymentId") String paymentId) {
        try
        {
            Integer status = paymentService.getPaymentStatus(paymentId);
            return ResponseEntity.ok(Map.of("status", status));
        }
        catch (Exception e)
        {
            log.error("Error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/member-pament-graph-data")
    public ResponseEntity<Map<String, Object>> getMemberPaymentGraphData(@RequestParam(name = "id") Long memberId)
    {
        try
        {
             Map<String, Object> data = paymentService.getMemberPaymentGraphData(memberId);
             return ResponseEntity.ok(data);
        }
        catch (Exception e)
        {
            log.error("Error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping(value = {"/delete/{id}"})
    public String delete(@PathVariable(required = true) Long id,
                         HttpServletRequest request,
                         RedirectAttributes redirectAttributes) {
        log.info("id: {}", id);
        try {
            paymentService.softDelete(id);
            redirectAttributes.addFlashAttribute("success", "Xóa thành công!");
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error in server!");
        }
        return AppUtils.goBack(request).orElse("redirect:/member/index");
    }

}
