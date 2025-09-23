package com.ringme.cms.service.gym;

import com.ringme.cms.dto.gym.MemberSubscriptionDto;
import com.ringme.cms.dto.gym.RecalculateDto;
import com.ringme.cms.model.gym.Member;
import com.ringme.cms.model.gym.MemberSubscription;
import com.ringme.cms.model.gym.Membership;
import com.ringme.cms.model.gym.Trainer;
import com.ringme.cms.repository.gym.MemberRepository;
import com.ringme.cms.repository.gym.MemberSubscriptionRepository;
import com.ringme.cms.repository.gym.MembershipRepository;
import com.ringme.cms.repository.gym.TrainerRepository;
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

    private final PaypalSubscriptionService  paypalSubscriptionService;

    private final TrainerRepository trainerRepository;

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
                List<MemberSubscription> curMembership = memberSubscriptionRepository.findByMemberIdAndTypeAndTrainer(member.getId(), membership.getType(), formDto.getTrainerId());
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
                if(formDto.getTrainerId()!=null)
                {
                    Trainer trainer = trainerRepository.findById(formDto.getTrainerId()).orElseThrow();
                    memberSubscription.setTrainer(trainer);
                }
                return memberSubscriptionRepository.save(memberSubscription);
            } else {
                memberSubscription = memberSubscriptionRepository.findById(formDto.getId()).orElseThrow();
                modelMapper.map(formDto, memberSubscription);
                memberSubscription.setStatus(formDto.getStatusSubscription());
                memberSubscription.setUpdatedAt(LocalDateTime.now());
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                String[] parts = formDto.getStartEndString().split(" - ");
                memberSubscription.setStartAt(LocalDate.parse(parts[0], formatter));
                memberSubscription.setEndAt(LocalDate.parse(parts[1], formatter));
                MemberSubscription returnData =  memberSubscriptionRepository.save(memberSubscription);
                updateStartEndDate(returnData.getMember().getId(), returnData.getMembership().getType(), returnData.getTrainer().getId());
                return returnData;
            }

        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public void softDelete(Long id) throws Exception{
        try {
            MemberSubscription memberSubscription = memberSubscriptionRepository.findById(id).orElseThrow();
            memberSubscription.setStatus(-1);
            memberSubscription.setUpdatedAt(LocalDateTime.now());
            memberSubscriptionRepository.save(memberSubscription);
            updateStartEndDate(memberRepository.findIdByMemberSubscriptionId(memberSubscription.getId()),memberSubscription.getMembership().getType(),memberSubscription.getTrainer() == null ? null : memberSubscription.getTrainer().getId());
            if(memberSubscription.getIsRecurring() == 1 && memberSubscription.getPaypalSubscriptionId().startsWith("I-"))
            {
                paypalSubscriptionService.cancelSubscription(memberSubscription.getPaypalSubscriptionId(), "User requested to cancel subscription.");
            }
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public void updateStartEndDate(Long memberId, Integer type,Long trainerId)
    {
        List<MemberSubscription> memberSubscriptionList = memberSubscriptionRepository.findByMemberIdAndTypeAndTrainer(memberId, type, trainerId);
        if(memberSubscriptionList.size() > 1)
        {
            memberSubscriptionList.sort(Comparator.comparing(MemberSubscription::getStartAt));
            LocalDate tempEnd =  memberSubscriptionList.get(0).getEndAt();
            for(int i = 1; i < memberSubscriptionList.size(); i++)
            {
                MemberSubscription memberSubscription = memberSubscriptionList.get(i);
                tempEnd = tempEnd.plusDays(1);
                if(memberSubscription.getStartAt() != tempEnd)
                {
                    memberSubscription.setStartAt(tempEnd);
                    tempEnd = tempEnd.plusMonths(memberSubscription.getMembership().getDuration());
                    memberSubscription.setEndAt(tempEnd);
                    memberSubscriptionRepository.save(memberSubscription);
                }
                else {
                    tempEnd = memberSubscription.getEndAt();
                }
            }
        }

    }

    public Long resub(Long id, Integer status)
    {
        MemberSubscription memberSubscription = memberSubscriptionRepository.findById(id).orElseThrow();
        memberSubscription.setStatus(1);
        Long memberId = memberRepository.findIdByMemberSubscriptionId(memberSubscription.getId());
        List<MemberSubscription> curMembership = memberSubscriptionRepository.findByMemberIdAndTypeAndTrainer(memberId, memberSubscription.getMembership().getType(), memberSubscription.getTrainer() == null ?  null : memberSubscription.getTrainer().getId());
        LocalDate newStartDate;
        LocalDate newEndDate;
        if(!curMembership.isEmpty())
        {
            Optional<LocalDate> maxEndAt = curMembership.stream()
                    .map(MemberSubscription::getEndAt)
                    .max(LocalDate::compareTo);
            newStartDate = maxEndAt.get().plusDays(1);
            newEndDate = newStartDate.plusMonths(memberSubscription.getMembership().getDuration());
        }
        else {
            newStartDate = LocalDate.now();
            newEndDate = newStartDate.plusMonths(memberSubscription.getMembership().getDuration());
        }
        memberSubscription.setStartAt(newStartDate);
        memberSubscription.setEndAt(newEndDate);
        memberSubscriptionRepository.save(memberSubscription);
        return memberId;
    }

    public void recalculate(RecalculateDto formDto) {
        List<MemberSubscription> memberSubscriptionList = memberSubscriptionRepository.findByMemberIdAndType(formDto.getId(), formDto.getType());
        if(!memberSubscriptionList.isEmpty())
        {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            memberSubscriptionList.sort(Comparator.comparing(MemberSubscription::getStartAt));
            memberSubscriptionList.get(0).setStartAt(LocalDate.parse(formDto.getDateString(), formatter));
            memberSubscriptionList.get(0).setEndAt(memberSubscriptionList.get(0).getStartAt().plusMonths(memberSubscriptionList.get(0).getMembership().getDuration()));
            memberSubscriptionRepository.save(memberSubscriptionList.get(0));
            LocalDate tempEnd =  memberSubscriptionList.get(0).getEndAt();
            for(int i = 1; i < memberSubscriptionList.size(); i++)
            {
                MemberSubscription memberSubscription = memberSubscriptionList.get(i);
                tempEnd = tempEnd.plusDays(1);
                if(memberSubscription.getStartAt() != tempEnd)
                {
                    memberSubscription.setStartAt(tempEnd);
                    tempEnd = tempEnd.plusMonths(memberSubscription.getMembership().getDuration());
                    memberSubscription.setEndAt(tempEnd);
                    memberSubscriptionRepository.save(memberSubscription);
                }
                else {
                    tempEnd = memberSubscription.getEndAt();
                }
            }
        }
    }
}

