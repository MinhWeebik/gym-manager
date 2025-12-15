package com.ringme.cms.service.gym;

import com.ringme.cms.dto.gym.AppointmentDto;
import com.ringme.cms.dto.gym.CalenderEventDto;
import com.ringme.cms.enums.RecurrenceType;
import com.ringme.cms.exception.ClassInSessionException;
import com.ringme.cms.model.gym.*;
import com.ringme.cms.repository.gym.ScheduledClassInstanceRepository;
import com.ringme.cms.repository.gym.ScheduledClassRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class ScheduledClassInstanceService {

    private final ScheduledClassInstanceRepository scheduledClassInstanceRepository;

    private final ScheduledClassRepository scheduledClassRepository;

    public List<ScheduledClassInstance> getAllInstances(LocalDate start, LocalDate end, Long trainerId) throws Exception{
        return scheduledClassInstanceRepository.getAllInstance(start, end, trainerId);
    }

    public void updateInstance(Long id, CalenderEventDto dto, String deleteType) throws Exception
    {
        ScheduledClassInstance scheduledClassInstance = scheduledClassInstanceRepository.findById(id).orElseThrow();
        LocalDateTime threshold = LocalDateTime.now().plusHours(12);
        log.info(threshold + "," + scheduledClassInstance.getDate().atTime(scheduledClassInstance.getFrom()));
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        if(threshold.isAfter(dto.getStartTime()) || threshold.isAfter(scheduledClassInstance.getDate().atTime(scheduledClassInstance.getFrom())))
        {
            throw new ClassInSessionException("Không thể di chuyển trước 12 tiếng");
        }
        if(deleteType.equalsIgnoreCase("one"))
        {
            scheduledClassInstance.setDate(dto.getStartTime().toLocalDate());
            scheduledClassInstance.setFrom(dto.getStartTime().toLocalTime());
            scheduledClassInstance.setTo(dto.getEndTime().toLocalTime());
            scheduledClassInstanceRepository.save(scheduledClassInstance);
        }
        else if(deleteType.equalsIgnoreCase("all"))
        {

            List<ScheduledClassInstance> scheduledClassInstances = scheduledClassInstanceRepository.findByAppointmentIdAndDate(scheduledClassInstance.getScheduledClass().getId(), scheduledClassInstance.getOriginalTime());
            RecurrenceType recurrenceType = scheduledClassInstance.getScheduledClass().getRepeat();
            LocalDate date = dto.getStartTime().toLocalDate();
            for(ScheduledClassInstance item : scheduledClassInstances)
            {
                switch (recurrenceType){
                    case WEEKLY: {
                        date = date.plusWeeks(1);
                        break;
                    }
                    case FORTNIGHTLY: {
                        date = date.plusWeeks(2);
                        break;
                    }
                    case THREE_WEEKLY: {
                        date = date.plusWeeks(3);
                        break;
                    }
                    case FOUR_WEEKLY: {
                        date = date.plusWeeks(4);
                        break;
                    }
                    case SIX_WEEKLY: {
                        date = date.plusWeeks(6);
                        break;
                    }
                    case EIGHT_WEEKLY: {
                        date = date.plusWeeks(8);
                        break;
                    }
                    case MONTHLY: {
                        date = date.plusMonths(1);
                        break;
                    }
                    case DAILY: {
                        date = date.plusDays(1);
                        break;
                    }
                }
                item.setDate(date);
                item.setFrom(dto.getStartTime().toLocalTime());
                item.setTo(dto.getEndTime().toLocalTime());
                scheduledClassInstanceRepository.save(item);
            }
            scheduledClassInstance.setDate(dto.getStartTime().toLocalDate());
            scheduledClassInstance.setFrom(dto.getStartTime().toLocalTime());
            scheduledClassInstance.setTo(dto.getEndTime().toLocalTime());
            scheduledClassInstanceRepository.save(scheduledClassInstance);
            ScheduledClass scheduledClass = scheduledClassInstance.getScheduledClass();
            if(scheduledClass.getEndRecur()!=null)
            {
                if(!scheduledClassInstances.isEmpty())
                {
                    ScheduledClassInstance lastInstance = scheduledClassInstances.get(scheduledClassInstances.size()-1);
                    scheduledClass.setEndRecur(lastInstance.getDate());
                    scheduledClassRepository.save(scheduledClass);
                }
            }
        }
        else {
            List<ScheduledClassInstance> scheduledClassInstances = scheduledClassInstanceRepository.findByScheduleIdAndThreshold(scheduledClassInstance.getScheduledClass().getId(), threshold.toLocalDate(), threshold.toLocalTime());
            for(ScheduledClassInstance item : scheduledClassInstances)
            {
                item.setFrom(dto.getStartTime().toLocalTime());
                item.setTo(dto.getEndTime().toLocalTime());
                scheduledClassInstanceRepository.save(item);
            }
            ScheduledClass scheduledClass = scheduledClassInstance.getScheduledClass();
            if(scheduledClass.getEndRecur()!=null)
            {
                if(!scheduledClassInstances.isEmpty())
                {
                    ScheduledClassInstance lastInstance = scheduledClassInstances.get(scheduledClassInstances.size()-1);
                    scheduledClass.setEndRecur(lastInstance.getDate());
                    scheduledClassRepository.save(scheduledClass);
                }
            }
        }
    }
}
