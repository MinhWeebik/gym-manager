package com.ringme.cms.service.music;

import com.ringme.cms.dto.gym.MemberDto;
import com.ringme.cms.dto.gym.MemberSubscriptionDto;
import com.ringme.cms.model.gym.Member;
import com.ringme.cms.model.gym.MemberSubscription;
import com.ringme.cms.repository.gym.MemberSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Log4j2
public class MemberSubscriptionService {

    private final MemberSubscriptionRepository memberSubscriptionRepository;

    private final ModelMapper modelMapper;

    public void save(MemberSubscriptionDto formDto) throws Exception {
        try {
            MemberSubscription memberSubscription;
            if (formDto.getId() == null) {
                memberSubscription = modelMapper.map(formDto, MemberSubscription.class);
                memberSubscription.setCreatedAt(LocalDateTime.now());
                memberSubscription.setUpdatedAt(LocalDateTime.now());
            } else {
                memberSubscription = memberSubscriptionRepository.findById(formDto.getId()).orElseThrow();
                modelMapper.map(formDto, memberSubscription);
                memberSubscription.setUpdatedAt(LocalDateTime.now());
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String[] parts = formDto.getStartEndString().split(" - ");
            memberSubscription.setStartAt(LocalDate.parse(parts[0], formatter));
            memberSubscription.setEndAt(LocalDate.parse(parts[1], formatter));

            memberSubscriptionRepository.save(memberSubscription);
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
