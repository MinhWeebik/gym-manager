package com.ringme.cms.service.gym;

import com.ringme.cms.dto.gym.MemberSubscriptionDto;
import com.ringme.cms.model.gym.Member;
import com.ringme.cms.model.gym.MemberSubscription;
import com.ringme.cms.model.gym.Membership;
import com.ringme.cms.repository.gym.MemberRepository;
import com.ringme.cms.repository.gym.MemberSubscriptionRepository;
import com.ringme.cms.repository.gym.MembershipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Log4j2
public class MemberSubscriptionService {

    private final MemberSubscriptionRepository memberSubscriptionRepository;

    private final ModelMapper modelMapper;

    private final MemberRepository memberRepository;

    private final MembershipRepository  membershipRepository;

    public MemberSubscription save(MemberSubscriptionDto formDto) throws Exception {
        try {
            MemberSubscription memberSubscription;
            if (formDto.getId() == null) {
                memberSubscription = modelMapper.map(formDto, MemberSubscription.class);
                memberSubscription.setCreatedAt(LocalDateTime.now());
                memberSubscription.setUpdatedAt(LocalDateTime.now());
                Member member = memberRepository.findById(formDto.getMemberId()).orElseThrow();
                memberSubscription.setMember(member);
                Membership membership = membershipRepository.findById(formDto.getMembershipId()).orElseThrow();
                memberSubscription.setMembership(membership);
                if(membership.getTotalVisit() != null && membership.getTotalVisit() != 0)
                {
                    memberSubscription.setNumberOfVisit(0);
                }
                else {
                    memberSubscription.setNumberOfVisit(null);
                }
                List<MemberSubscription> curMembership = memberSubscriptionRepository.findByMemberIdAndType(member.getId(), membership.getType());
                LocalDate newStartDate;
                LocalDate newEndDate;
                if(!curMembership.isEmpty())
                {
                    Optional<LocalDate> maxEndAt = curMembership.stream()
                            .map(MemberSubscription::getEndAt)
                            .max(LocalDate::compareTo);
                    newStartDate = maxEndAt.get().plusDays(1);
                    newEndDate = newStartDate.plusMonths(membership.getDuration());
                }
                else {
                    newStartDate = LocalDate.now();
                    newEndDate = newStartDate.plusMonths(membership.getDuration());
                }
                memberSubscription.setStartAt(newStartDate);
                memberSubscription.setEndAt(newEndDate);
                if(formDto.getPaymentGateway().equals("cash"))
                {
                    memberSubscription.setStatus(1);
                }
                if(formDto.getPaymentGateway().equals("paypal"))
                {
                    memberSubscription.setStatus(2);
                }
            } else {
                memberSubscription = memberSubscriptionRepository.findById(formDto.getId()).orElseThrow();
                modelMapper.map(formDto, memberSubscription);
                memberSubscription.setUpdatedAt(LocalDateTime.now());
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                String[] parts = formDto.getStartEndString().split(" - ");
                memberSubscription.setStartAt(LocalDate.parse(parts[0], formatter));
                memberSubscription.setEndAt(LocalDate.parse(parts[1], formatter));
            }

            return memberSubscriptionRepository.save(memberSubscription);
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
