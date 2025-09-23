package com.ringme.cms.service.gym;

import com.ringme.cms.dto.gym.AppointmentDto;
import com.ringme.cms.dto.gym.CalenderEventDto;
import com.ringme.cms.enums.RecurrenceType;
import com.ringme.cms.exception.ClassInSessionException;
import com.ringme.cms.model.gym.Appointment;
import com.ringme.cms.model.gym.AppointmentInstance;
import com.ringme.cms.model.gym.MemberSubscription;
import com.ringme.cms.model.gym.ScheduledClass;
import com.ringme.cms.repository.gym.AppointmentInstanceRepository;
import com.ringme.cms.repository.gym.AppointmentRepository;
import com.ringme.cms.repository.gym.MemberSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class AppointmentInstanceService {

    private final AppointmentInstanceRepository appointmentInstanceRepository;
    private final MemberSubscriptionRepository memberSubscriptionRepository;
    private final AppointmentService appointmentService;
    private final AppointmentRepository appointmentRepository;

    public List<AppointmentInstance> getAllAppointmentService(LocalDate start, LocalDate end, Long trainerId) throws Exception{
        return appointmentInstanceRepository.getAllAppointmentInstance(start, end, trainerId);
    }

    public void updateAppointmentInstance(Long id, CalenderEventDto dto, String deleteType) throws Exception
    {
        AppointmentInstance appointmentInstance = appointmentInstanceRepository.findById(id).orElseThrow();
        LocalDateTime threshold = LocalDateTime.now().plusHours(12);
        log.info(threshold + "," + appointmentInstance.getDate().atTime(appointmentInstance.getFrom()));
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        if(threshold.isAfter(dto.getStartTime()) || threshold.isAfter(appointmentInstance.getDate().atTime(appointmentInstance.getFrom())))
        {
            throw new ClassInSessionException("Không thể di chuyển trước 12 tiếng");
        }
        if(deleteType.equalsIgnoreCase("one"))
        {
            appointmentInstance.setDate(dto.getStartTime().toLocalDate());
            appointmentInstance.setFrom(dto.getStartTime().toLocalTime());
            appointmentInstance.setTo(dto.getEndTime().toLocalTime());
            appointmentInstanceRepository.save(appointmentInstance);
        }
        else if(deleteType.equalsIgnoreCase("all"))
        {
//            List<AppointmentInstance> appointmentInstances = appointmentInstanceRepository.findByAppointmentIdAndDate(appointmentInstance.getAppointment().getId(), appointmentInstance.getOriginalTime());
//            RecurrenceType recurrenceType = appointmentInstance.getAppointment().getRepeat();
//            LocalDate date = dto.getStartTime().toLocalDate();
//            for(AppointmentInstance item : appointmentInstances)
//            {
//                switch (recurrenceType){
//                    case WEEKLY: {
//                        date = date.plusWeeks(1);
//                        break;
//                    }
//                    case FORTNIGHTLY: {
//                        date = date.plusWeeks(2);
//                        break;
//                    }
//                    case THREE_WEEKLY: {
//                        date = date.plusWeeks(3);
//                        break;
//                    }
//                    case FOUR_WEEKLY: {
//                        date = date.plusWeeks(4);
//                        break;
//                    }
//                    case SIX_WEEKLY: {
//                        date = date.plusWeeks(6);
//                        break;
//                    }
//                    case EIGHT_WEEKLY: {
//                        date = date.plusWeeks(8);
//                        break;
//                    }
//                    case MONTHLY: {
//                        date = date.plusMonths(1);
//                        break;
//                    }
//                    case DAILY: {
//                        date = date.plusDays(1);
//                        break;
//                    }
//                }
//                item.setDate(date);
//                item.setFrom(dto.getStartTime().toLocalTime());
//                item.setTo(dto.getEndTime().toLocalTime());
//                appointmentInstanceRepository.save(item);
//            }
//            appointmentInstance.setDate(dto.getStartTime().toLocalDate());
//            appointmentInstance.setFrom(dto.getStartTime().toLocalTime());
//            appointmentInstance.setTo(dto.getEndTime().toLocalTime());
//            appointmentInstanceRepository.save(appointmentInstance);
            Appointment appointment = appointmentInstance.getAppointment();
            List<MemberSubscription> memberSubscriptions = memberSubscriptionRepository.findForBooking(appointment.getTrainer().getId(), appointment.getMember().getId());
            List<AppointmentInstance> appointmentInstances = appointmentInstanceRepository.findByAppointmentIdAndDate(appointmentInstance.getAppointment().getId(), appointmentInstance.getOriginalTime());
            int count = 1;
            for(AppointmentInstance item : appointmentInstances)
            {
                appointmentInstanceRepository.delete(item);
                count++;
            }
            for (int i = memberSubscriptions.size() - 1; i >= 0; i--)
            {
                MemberSubscription item = memberSubscriptions.get(i);
                if(count > 0)
                {
                    if(count >= item.getNumberOfVisit())
                    {
                        count = count - item.getNumberOfVisit();
                        item.setNumberOfVisit(0);
                    }
                    else {
                        item.setNumberOfVisit(item.getNumberOfVisit() - count);
                        count = 0;
                    }
                    memberSubscriptionRepository.save(item);
                }
            }
            appointmentInstanceRepository.delete(appointmentInstance);
            AppointmentDto formDto = new AppointmentDto();
            formDto.setStartDateStr(dto.getStartTime().toLocalDate().format(dateFormatter));
            formDto.setFromStr(dto.getStartTime().toLocalTime().format(timeFormatter));
            formDto.setToStr(dto.getEndTime().toLocalTime().format(timeFormatter));
            formDto.setRepeat(appointment.getRepeat().toString());
            formDto.setEndRecurStr(appointment.getEndRecur().format(dateFormatter));
            formDto.setTrainerId(appointment.getTrainer().getId());
            formDto.setMemberId(appointment.getMember().getId());
            appointmentService.addAppointmentInstances(formDto, appointment.getMember(), appointment);
        }
        else {
//            List<AppointmentInstance> appointmentInstances = appointmentInstanceRepository.findByAppointmentIdAndThreshold(appointmentInstance.getAppointment().getId(), threshold.toLocalDate(), threshold.toLocalTime());
//            for(AppointmentInstance item : appointmentInstances)
//            {
//                item.setFrom(dto.getStartTime().toLocalTime());
//                item.setTo(dto.getEndTime().toLocalTime());
//                appointmentInstanceRepository.save(item);
//            }
            Appointment appointment = appointmentInstance.getAppointment();
            List<MemberSubscription> memberSubscriptions = memberSubscriptionRepository.findForBooking(appointment.getTrainer().getId(), appointment.getMember().getId());
            List<AppointmentInstance> appointmentInstances = appointmentInstanceRepository.findByAppointmentIdAndThreshold(appointmentInstance.getAppointment().getId(), threshold.toLocalDate(), threshold.toLocalTime());
            LocalDate startDate = appointmentInstances.get(0).getDate();
            int count = 0;
            for(AppointmentInstance item : appointmentInstances)
            {
                appointmentInstanceRepository.delete(item);
                count++;
            }
            for (int i = memberSubscriptions.size() - 1; i >= 0; i--)
            {
                MemberSubscription item = memberSubscriptions.get(i);
                if(count > 0)
                {
                    if(count >= item.getNumberOfVisit())
                    {
                        count = count - item.getNumberOfVisit();
                        item.setNumberOfVisit(0);
                    }
                    else {
                        item.setNumberOfVisit(item.getNumberOfVisit() - count);
                        count = 0;
                    }
                    memberSubscriptionRepository.save(item);
                }
            }
            AppointmentDto formDto = new AppointmentDto();
            formDto.setStartDateStr(startDate.format(dateFormatter));
            formDto.setFromStr(dto.getStartTime().toLocalTime().format(timeFormatter));
            formDto.setToStr(dto.getEndTime().toLocalTime().format(timeFormatter));
            formDto.setRepeat(appointment.getRepeat().toString());
            String endRecurStr = null;
            if(appointment.getEndRecur()!=null)
            {
                endRecurStr = appointment.getEndRecur().format(dateFormatter);
            }
            formDto.setEndRecurStr(endRecurStr);
            formDto.setTrainerId(appointment.getTrainer().getId());
            formDto.setMemberId(appointment.getMember().getId());
            appointmentService.addAppointmentInstances(formDto, appointment.getMember(), appointment);
        }

    }
}
