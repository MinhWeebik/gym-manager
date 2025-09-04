package com.ringme.cms.service.gym;

import com.ringme.cms.model.gym.Member;
import com.ringme.cms.model.gym.MemberSubscription;
import com.ringme.cms.repository.gym.MemberRepository;
import com.ringme.cms.repository.gym.MemberSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Component
@Log4j2
@RequiredArgsConstructor
@Transactional
public class ScheduleService {

    private final MemberSubscriptionRepository memberSubscriptionRepository;

    private final PaypalSubscriptionService paypalSubscriptionService;
    private final MemberRepository memberRepository;
    private final MemberSubscriptionService memberSubscriptionService;

//    @Scheduled(cron = "0 * * * * ?")
    @Scheduled(cron = "0 0 1 * * ?")
    public void checkExpirationDate() {
        log.info("Checking expiration date at {}", LocalDateTime.now());
        try
        {
            List<MemberSubscription> cancelledSubscription = memberSubscriptionRepository.findAllCancelledSubscription();
            if (!cancelledSubscription.isEmpty()) {
                for (MemberSubscription memberSubscription : cancelledSubscription) {
                    paypalSubscriptionService.cancelSubscription(memberSubscription.getPaypalSubscriptionId(), "User requested to cancel subscription.");
                }
            }
            memberSubscriptionRepository.autoUpdateStatus();
            log.info("Successfully auto update status at {}", LocalDateTime.now());
        }
        catch (Exception e)
        {
            log.error("Error while checking expiration date at {}", LocalDateTime.now(), e);
        }
    }

//    @Scheduled(cron = "0 * * * * ?")
    @Scheduled(cron = "0 0 1 * * ?")
    public void checkPendingPaymentsAndSubscriptions() {
        log.info("Checking pending payment and subscriptions date at {}", LocalDateTime.now());
        try
        {
            List<Long> idsToUpdate = memberSubscriptionRepository.findIdsOfPendingSubscriptionsToCancel();
            if (idsToUpdate != null && !idsToUpdate.isEmpty()) {
                memberSubscriptionRepository.updateStatusForIds(idsToUpdate);
                for(Long id : idsToUpdate)
                {
                    MemberSubscription memberSubscription = memberSubscriptionRepository.findById(id).orElse(null);
                    if(memberSubscription != null)
                    {
                        Long memberId = memberRepository.findIdByMemberSubscriptionId(id);
                        memberSubscriptionService.updateStartEndDate(memberId, memberSubscription.getMembership().getType());
                    }
                }
            }
            memberSubscriptionRepository.autoUpdatePendingPayment();
            log.info("Successfully auto update pending payment and subscriptions at {}", LocalDateTime.now());
        }
        catch (Exception e)
        {
            log.error("Error while checking pending payment and subscriptions at {}", LocalDateTime.now(), e);
        }
    }
}
