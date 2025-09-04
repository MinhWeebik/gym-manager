package com.ringme.cms.controller.gym;

import com.fasterxml.jackson.databind.JsonNode;
import com.ringme.cms.model.gym.Member;
import com.ringme.cms.model.gym.MemberSubscription;
import com.ringme.cms.repository.gym.MemberRepository;
import com.ringme.cms.repository.gym.MemberSubscriptionRepository;
import com.ringme.cms.repository.gym.PaymentRepository;
import com.ringme.cms.service.gym.PaypalSubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Controller
@Log4j2
@RequestMapping("/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final PaypalSubscriptionService paypalSubscriptionService;
    private final PaymentRepository  paymentRepository;
    private final MemberSubscriptionRepository memberSubscriptionRepository;
    private final MemberRepository memberRepository;

    @GetMapping("/success")
    public String subscriptionSuccess(@RequestParam("subscription_id") String subscriptionId) {
        try {
            JsonNode subscriptionDetails = paypalSubscriptionService.getSubscriptionDetails(subscriptionId);
            String status = subscriptionDetails.path("status").asText();

            // 2. Check the status and update your database
            if ("ACTIVE".equalsIgnoreCase(status)) {
                com.ringme.cms.model.gym.Payment paymentModel = paymentRepository.findByGatewayTransactionId(subscriptionId);
                if (paymentModel != null) {
                    paymentModel.setStatus(1);
                    paymentRepository.save(paymentModel);
                }
                MemberSubscription memberSubscription = memberSubscriptionRepository.findByPaypalSubscriptionId(subscriptionId);
                if (memberSubscription != null) {
                    memberSubscription.setStatus(1);
                    memberSubscriptionRepository.save(memberSubscription);
                }
                return "gym/payment/success";
            } else {
                com.ringme.cms.model.gym.Payment paymentModel = paymentRepository.findByGatewayTransactionId(subscriptionId);
                if (paymentModel != null) {
                    paymentModel.setStatus(-1);
                    paymentRepository.save(paymentModel);
                }
                MemberSubscription memberSubscription = memberSubscriptionRepository.findByPaypalSubscriptionId(subscriptionId);
                if (memberSubscription != null) {
                    memberSubscription.setStatus(-1);
                    memberSubscriptionRepository.save(memberSubscription);
                }
                return "gym/payment/cancel";
            }
        } catch (Exception e) {
            return "gym/payment/cancel";
        }
    }

    @GetMapping("/cancel/{id}")
    public String subscriptionCancel(@PathVariable Long id) {
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

    @PostMapping("/pause/{id}")
    public String pauseSubscription(@PathVariable Long id, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        MemberSubscription memberSubscription = memberSubscriptionRepository.findById(id).orElseThrow();
        Long memberId = memberRepository.findIdByMemberSubscriptionId(id);
        String subscriptionId = memberSubscription.getPaypalSubscriptionId();
        try {
            memberSubscription.setIsRecurring(0);
            memberSubscriptionRepository.save(memberSubscription);
            paypalSubscriptionService.suspendSubscription(subscriptionId, "User requested cancellation at period end.");
            redirectAttributes.addFlashAttribute("success", "Gói thành viên của bạn sẽ tự hủy khi hết hạn.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("success", "Không thể hủy gói thành viên");
        }
        return "redirect:/member/detail?id=" + memberId + "&tab=2";
    }

    @PostMapping("/resume/{id}")
    public String resumeSubscription(@PathVariable Long id, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        MemberSubscription memberSubscription = memberSubscriptionRepository.findById(id).orElseThrow();
        Long memberId = memberRepository.findIdByMemberSubscriptionId(id);
        String subscriptionId = memberSubscription.getPaypalSubscriptionId();
        try {
            memberSubscription.setIsRecurring(1);
            memberSubscriptionRepository.save(memberSubscription);
            paypalSubscriptionService.activateSubscription(subscriptionId, "User requested to resume subscription.");
            redirectAttributes.addFlashAttribute("success", "Gói thành viên định kỳ đã được kích hoạt lại.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("success", "Không thể kích hoạt định kỳ gói thành viên");
        }
        return "redirect:/member/detail?id=" + memberId + "&tab=2";
    }
}
