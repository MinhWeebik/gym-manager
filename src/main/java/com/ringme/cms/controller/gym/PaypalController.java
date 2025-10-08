package com.ringme.cms.controller.gym;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ringme.cms.dto.gym.PaymentDto;
import com.ringme.cms.model.gym.MemberSubscription;
import com.ringme.cms.model.gym.Payment;
import com.ringme.cms.repository.gym.MemberRepository;
import com.ringme.cms.repository.gym.MemberSubscriptionRepository;
import com.ringme.cms.service.gym.PaymentService;
import com.ringme.cms.service.gym.PaypalSubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Controller
@Log4j2
@RequestMapping("/paypal")
@RequiredArgsConstructor
public class PaypalController
{

    private final MemberSubscriptionRepository  memberSubscriptionRepository;

    private final PaypalSubscriptionService paypalSubscriptionService;

    private final PaymentService paymentService;
    private final MemberRepository memberRepository;
    private final PaypalSubscriptionService payPalSubscriptionService;
    @Value("${paypal.product.id}")
    private String DEFAULT_PRODUCT_ID;

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestHeader Map<String, String> headers, @RequestBody String payload) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode event = objectMapper.readTree(payload);
            String eventType = event.get("event_type").asText();

            if ("PAYMENT.SALE.COMPLETED".equals(eventType)) {
                log.info("Payment sale completed event received.");
                JsonNode resource = event.get("resource");
                String subscriptionId = resource.get("billing_agreement_id").asText();
                if(subscriptionId.contains("I-"))
                {
                    MemberSubscription memberSubscription = memberSubscriptionRepository.findByPaypalSubscriptionId(subscriptionId);
                    long daysBetween = ChronoUnit.DAYS.between(LocalDate.now(), memberSubscription.getEndAt());
                    if (daysBetween <= 5){
                        PaymentDto paymentDto = new PaymentDto();
                        paymentDto.setMemberId(memberRepository.findIdByMemberSubscriptionId(memberSubscription.getId()));
                        paymentDto.setDescription(String.format("Thanh toán định kỳ cho gói " + memberSubscription.getMembership().getName()));
                        paymentDto.setAmount(memberSubscription.getMembership().getPrice());
                        paymentDto.setPaymentGateway("paypal");
                        paymentDto.setType(1);
                        Payment curPayment = paymentService.save(paymentDto);
                        memberSubscription.setStatus(1);
                        memberSubscription.setStartAt(LocalDate.now());
                        memberSubscription.setEndAt(LocalDate.now().plusMonths(memberSubscription.getMembership().getDuration()));
                        memberSubscriptionRepository.save(memberSubscription);
                    }
                }
            }
            else if ("BILLING.SUBSCRIPTION.CANCELLED".equals(eventType)) {
                log.info("Subscription cancelled event received.");
                String subscriptionId = event.get("resource").get("id").asText();
                MemberSubscription memberSubscription = memberSubscriptionRepository.findByPaypalSubscriptionId(subscriptionId);
                memberSubscription.setIsRecurring(0);
                memberSubscriptionRepository.save(memberSubscription);
            }
            else if ("BILLING.SUBSCRIPTION.PAYMENT.FAILED".equals(eventType)) {
                log.warn("Subscription payment failed event received.");
                String subscriptionId = event.get("resource").get("id").asText();
                MemberSubscription memberSubscription = memberSubscriptionRepository.findByPaypalSubscriptionId(subscriptionId);
                memberSubscription.setIsRecurring(1);
                memberSubscription.setStatus(3);
                memberSubscriptionRepository.save(memberSubscription);
            }
            else if ("BILLING.SUBSCRIPTION.SUSPENDED".equals(eventType)) {
                log.error("All payment retries have failed. It is now suspended.");
                String subscriptionId = event.get("resource").get("id").asText();
                MemberSubscription memberSubscription = memberSubscriptionRepository.findByPaypalSubscriptionId(subscriptionId);
                if(memberSubscription.getIsRecurring() == 1 && memberSubscription.getStatus() == 3)
                {
                    memberSubscription.setStatus(0);
                    memberSubscriptionRepository.save(memberSubscription);
                    paypalSubscriptionService.cancelSubscription(memberSubscription.getPaypalSubscriptionId(), "All payment retries have failed. It is now cancelled");

                }
            }

        } catch (JsonProcessingException e) {
            log.error("Error processing JSON payload", e);
            return new ResponseEntity<>("Error processing request", HttpStatus.BAD_REQUEST);
        }
        catch (Exception e) {
            log.error("Error processing request", e);
        }

        return new ResponseEntity<>("Webhook processed", HttpStatus.OK);
    }

    @PostMapping("/create-plan")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleCreatePlan(@RequestParam String planName,
                                   @RequestParam String price,
                                   @RequestParam Integer duration) {
        try {
            String decodedPlanName = URLDecoder.decode(planName, StandardCharsets.UTF_8.name());
            String newPlanId = payPalSubscriptionService.createPlan(
                    DEFAULT_PRODUCT_ID,
                    decodedPlanName,
                    price,
                    "USD",
                    "MONTH",
                    duration
            );
            Map<String, Object> map = new HashMap<>();
            map.put("planId", newPlanId);
            return ResponseEntity.ok(map);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/deactivate-plan")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleDeactivePlan(@RequestParam String planId) {
        try {
            paypalSubscriptionService.deactivatePlan(planId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/update-plan-price")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleUpdatePlanPrice(@RequestParam String planId,
                                                                @RequestParam String price) {
        try {
            Boolean status = paypalSubscriptionService.updatePlanPrice(planId, price, "USD");
            if(status)
            {
                return ResponseEntity.ok().build();
            }
            else{
                return ResponseEntity.badRequest().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }


}
