package com.ringme.cms.service.gym;

import com.github.f4b6a3.uuid.UuidCreator;
import com.ringme.cms.common.Helper;
import com.ringme.cms.common.UploadFile;
import com.ringme.cms.dto.AjaxSearchDto;
import com.ringme.cms.dto.gym.MemberDto;
import com.ringme.cms.model.gym.Member;
import com.ringme.cms.model.gym.MemberSubscription;
import com.ringme.cms.repository.gym.MemberRepository;
import com.ringme.cms.repository.gym.MemberSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
public class MemberService {

    private final MemberRepository memberRepository;

    private final MemberSubscriptionRepository memberSubscriptionRepository;

    private final ModelMapper modelMapper;

    private final UploadFile uploadFile;

    public Page<Member> getPage(String name, Integer status, Integer gender,String email,
                                String phoneNumber, String orderBy, String member, Integer pageNo,Integer pageSize)
    {
        List<String> orderByList = List.of("updated_at", "created_at", "last_name asc", "last_name desc");
        if(orderBy!= null && !orderByList.contains(orderBy))
        {
            throw new IllegalArgumentException("Order by value not valid");
        }
        Pageable pageable;
        if(orderBy.startsWith("last_name"))
        {
            String orderDirection = orderBy.split(" ")[1];
            if(orderDirection.equals("desc"))
            {
                pageable = PageRequest.of(pageNo-1, pageSize, Sort.by("last_name").descending());
            }
            else
            {
                pageable = PageRequest.of(pageNo-1, pageSize, Sort.by("last_name").ascending());
            }
        }
        else
        {
            pageable = PageRequest.of(pageNo-1, pageSize, Sort.by(orderBy).descending());
        }
        if(member.equals("all"))
        {
            return memberRepository.findAll(name, status, gender, email, phoneNumber, pageable);
        }
        else if(member.equals("member"))
        {
            return memberRepository.findAllWithMembership(name, status, gender, email, phoneNumber, pageable);
        }
        else if(member.equals("notMember"))
        {
            return memberRepository.findAllWithNoMembership(name, status, gender, email, phoneNumber, pageable);
        }
        return memberRepository.findAll(name, status, gender, email, phoneNumber, pageable);
    }

    public List<Member> getMembershipData(List<Member> memberList, String memberKey)
    {
        memberList.forEach(member -> {
            if(!memberKey.equals("notMember"))
            {
                List<MemberSubscription> activeMemberSub = memberSubscriptionRepository.getActiveSubscription(member.getId());
                if(activeMemberSub != null && !activeMemberSub.isEmpty())
                {
                    member.setMemberSubscriptions(List.of(activeMemberSub.get(0)));
                }
                else {
                    member.setMemberSubscriptions(null);
                }
            }
            else {
                member.setMemberSubscriptions(null);
            }
        });
        return memberList;
    }

    public Member getMembershipData(Member member)
    {
        List<MemberSubscription> activeMemberSub = memberSubscriptionRepository.getActiveSubscription(member.getId());
        member.setMemberSubscriptions(activeMemberSub);
        return member;
    }

    public void save(MemberDto formDto) throws Exception {
        try {
            Member member;
            if (formDto.getId() == null) {
                member = modelMapper.map(formDto, Member.class);
                member.setStatus(1);
                member.setCreatedAt(LocalDateTime.now());
                member.setUpdatedAt(LocalDateTime.now());
                UUID newId = UuidCreator.getTimeOrderedEpoch();
                member.setUuid(newId.toString());
            } else {
                member = memberRepository.findById(formDto.getId()).orElseThrow();
                modelMapper.map(formDto, member);
                member.setUpdatedAt(LocalDateTime.now());
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            member.setDateOfBirth(LocalDate.parse(formDto.getDateOfBirthString(), formatter));

            if (formDto.getImageUpload() != null && !formDto.getImageUpload().isEmpty()) {
                Path fileName = uploadFile.createImageFile(formDto.getImageUpload(), "member");
                member.setImageUrl(File.separator + fileName);
            }
            memberRepository.save(member);
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public Page<Member> search(String name, String email, String phoneNumber, Integer gender, List<Long> exceptIds, Integer pageNo, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize);
        boolean isEmpty = false;
        if (exceptIds == null || exceptIds.isEmpty()) {
            isEmpty = true;
        }
        return memberRepository.search(name,gender,email,phoneNumber,isEmpty,exceptIds,pageable);
    }

    public List<AjaxSearchDto> ajaxSearchMember(String input, Long trainerId) {
        return Helper.listAjaxMember(memberRepository.ajaxSearchMember(Helper.processStringSearch(input), trainerId));
    }

}
