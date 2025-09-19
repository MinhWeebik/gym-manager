package com.ringme.cms.service.gym;

import com.ringme.cms.dto.gym.BlockedTimeDto;
import com.ringme.cms.dto.gym.CalenderEventDto;
import com.ringme.cms.dto.gym.ScheduleDto;
import com.ringme.cms.enums.RecurrenceType;
import com.ringme.cms.model.gym.BlockedTime;
import com.ringme.cms.model.gym.ScheduledClass;
import com.ringme.cms.repository.gym.BlockedTimeRepository;
import com.ringme.cms.repository.gym.TrainerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.cglib.core.Block;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Log4j2
@RequiredArgsConstructor
public class BlockedTimeService {

    private final BlockedTimeRepository blockedTimeRepository;

    private final ModelMapper modelMapper;

    private final TrainerRepository trainerRepository;

    public List<BlockedTime> getAllNonRepeated(LocalDate start, LocalDate end){
        return blockedTimeRepository.getAllNonRepeate(start,end);
    }

    public List<BlockedTime> getAllRepeated(LocalDate start, LocalDate end){
        return blockedTimeRepository.getAllRepeated(start,end);
    }

    public void updateBlockedTime(Long id, CalenderEventDto dto) throws Exception
    {
        BlockedTime blockedTime = blockedTimeRepository.findById(id).orElseThrow();
        if(blockedTime.getRepeat() != RecurrenceType.NONE)
        {
            if(blockedTime.getFrom().equals(dto.getStartTime().toLocalTime()) && blockedTime.getTo().equals(dto.getEndTime().toLocalTime()))
            {
                LocalDate updatedDate = dto.getStartTime().toLocalDate();
                blockedTime.setDate(updatedDate);
            }
            else {
                blockedTime.setFrom(dto.getStartTime().toLocalTime());
                blockedTime.setTo(dto.getEndTime().toLocalTime());
            }
        }
        else {
            LocalDate updatedDate = dto.getStartTime().toLocalDate();
            blockedTime.setDate(updatedDate);
            blockedTime.setFrom(dto.getStartTime().toLocalTime());
            blockedTime.setTo(dto.getEndTime().toLocalTime());
        }
        blockedTimeRepository.save(blockedTime);
    }

    public void save(BlockedTimeDto formDto) throws Exception {
        try {
            BlockedTime blockedTime;
            if (formDto.getId() == null) {
                blockedTime = modelMapper.map(formDto, BlockedTime.class);
            } else {
                blockedTime = blockedTimeRepository.findById(formDto.getId()).orElseThrow();
                modelMapper.map(formDto, blockedTime);
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            blockedTime.setDate(LocalDate.parse(formDto.getStartDateStr(), formatter));
            if(formDto.getEndRecurStr() != null &&!formDto.getEndRecurStr().isEmpty())
            {
                blockedTime.setEndRecur(LocalDate.parse(formDto.getEndRecurStr(), formatter));
            }
            else blockedTime.setEndRecur(null);
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            blockedTime.setFrom(LocalTime.parse(formDto.getFromStr(), timeFormatter));
            blockedTime.setTo(LocalTime.parse(formDto.getToStr(), timeFormatter));
            if(formDto.getTrainerId() != null)
            {
                blockedTime.setTrainer(trainerRepository.findById(formDto.getTrainerId()).orElseThrow());
            }

            blockedTimeRepository.save(blockedTime);
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

}
