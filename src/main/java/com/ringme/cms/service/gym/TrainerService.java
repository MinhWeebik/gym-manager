package com.ringme.cms.service.gym;

import com.github.f4b6a3.uuid.UuidCreator;
import com.ringme.cms.common.Helper;
import com.ringme.cms.common.UploadFile;
import com.ringme.cms.dto.AjaxSearchDto;
import com.ringme.cms.dto.gym.MemberDto;
import com.ringme.cms.dto.gym.TrainerDto;
import com.ringme.cms.model.gym.*;
import com.ringme.cms.repository.gym.MemberSubscriptionRepository;
import com.ringme.cms.repository.gym.TrainerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
public class TrainerService {

    private final TrainerRepository trainerRepository;

    private final ModelMapper modelMapper;

    private final UploadFile uploadFile;

    private final MemberSubscriptionRepository  memberSubscriptionRepository;

    public List<AjaxSearchDto> ajaxSearchTrainer(String input) {
        return Helper.listAjax(trainerRepository.ajaxSearchTrainer(Helper.processStringSearch(input)),1);
    }

    public Page<Trainer> getPage(String name, Integer status, Integer gender, String email, String phoneNumber, Integer pageNo, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize);
        return trainerRepository.findAll(name,status,gender,email,phoneNumber,pageable);
    }

    public void save(TrainerDto formDto) throws Exception {
        try {
            Trainer trainer;
            if (formDto.getId() == null) {
                trainer = modelMapper.map(formDto, Trainer.class);
                trainer.setStatus(1);
                trainer.setCreatedAt(LocalDateTime.now());
                trainer.setUpdatedAt(LocalDateTime.now());
            } else {
                trainer = trainerRepository.findById(formDto.getId()).orElseThrow();
                modelMapper.map(formDto, trainer);
                trainer.setUpdatedAt(LocalDateTime.now());
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            trainer.setDateOfBirth(LocalDate.parse(formDto.getDateOfBirthString(), formatter));
            trainer.setHireDate(LocalDateTime.parse(formDto.getHireDateString(), dateTimeFormatter));

            if (formDto.getImageUpload() != null && !formDto.getImageUpload().isEmpty()) {
                Path fileName = uploadFile.createImageFile(formDto.getImageUpload(), "trainer");
                trainer.setImageUrl(File.separator + fileName);
            }
            trainerRepository.save(trainer);
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public void softDelete(Long id) throws Exception{
        Trainer trainer = trainerRepository.findById(id).orElseThrow(() -> new Exception("Không tìm thấy HLV"));
        List<MemberSubscription> memberSubscriptions = memberSubscriptionRepository.findByTrainerId(trainer.getId());
        if(!memberSubscriptions.isEmpty())
        {
            throw new Exception("Đang có thành viên đăng ký HLV này!");
        }
        trainer.setStatus(0);
        trainerRepository.save(trainer);
    }
}
