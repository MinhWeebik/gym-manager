package com.ringme.cms.service.gym;

import com.ringme.cms.dto.gym.AppointmentDto;
import com.ringme.cms.dto.gym.CalenderEventDto;
import com.ringme.cms.dto.gym.ScheduleDto;
import com.ringme.cms.enums.RecurrenceType;
import com.ringme.cms.model.gym.*;
import com.ringme.cms.repository.gym.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final ModelMapper modelMapper;
    private final MemberRepository memberRepository;
    private final TrainerRepository trainerRepository;
    private final MemberSubscriptionRepository memberSubscriptionRepository;
    private final AppointmentInstanceRepository appointmentInstanceRepository;

    public List<Appointment> getAllAppointment(LocalDate start, LocalDate end, Long trainerId) throws Exception{
        return  appointmentRepository.getAllNoneRepeat(start, end, trainerId);
    }

    public List<Appointment> getAllRepeatedAppointment(LocalDate start, LocalDate end, Long trainerId) throws Exception{
        return appointmentRepository.getAllRepeat(start, end, trainerId);
    }

    public void updateAppointment(Long id, CalenderEventDto dto) throws Exception
    {
        Appointment appointment = appointmentRepository.findById(id).orElseThrow();
        if(appointment.getRepeat() != RecurrenceType.NONE)
        {
            if(appointment.getFrom().equals(dto.getStartTime().toLocalTime()) && appointment.getTo().equals(dto.getEndTime().toLocalTime()))
            {
                LocalDate updatedDate = dto.getStartTime().toLocalDate();
                appointment.setDate(updatedDate);
            }
            else {
                appointment.setFrom(dto.getStartTime().toLocalTime());
                appointment.setTo(dto.getEndTime().toLocalTime());
            }
        }
        else {
            LocalDate updatedDate = dto.getStartTime().toLocalDate();
            appointment.setDate(updatedDate);
            appointment.setFrom(dto.getStartTime().toLocalTime());
            appointment.setTo(dto.getEndTime().toLocalTime());
        }
        appointmentRepository.save(appointment);
    }

    public void save(AppointmentDto formDto) throws Exception {
        try {
            Member member = memberRepository.findById(formDto.getMemberId()).orElseThrow();
            Appointment appointment;
            if (formDto.getId() == null) {
                appointment = modelMapper.map(formDto, Appointment.class);
                appointment.setStatus(1);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                appointment.setDate(LocalDate.parse(formDto.getStartDateStr(), formatter));
                if(formDto.getEndRecurStr() != null && !formDto.getEndRecurStr().isEmpty())
                {
                    appointment.setEndRecur(LocalDate.parse(formDto.getEndRecurStr(), formatter));
                }
                else appointment.setEndRecur(null);
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
                appointment.setFrom(LocalTime.parse(formDto.getFromStr(), timeFormatter));
                appointment.setTo(LocalTime.parse(formDto.getToStr(), timeFormatter));
                appointment.setMember(member);
                appointment.setTrainer(trainerRepository.findById(formDto.getTrainerId()).orElseThrow());
                Appointment savedAppointment = appointmentRepository.save(appointment);
                addAppointmentInstances(formDto, member, savedAppointment);
            } else {
                appointment = appointmentRepository.findById(formDto.getId()).orElseThrow();
                modelMapper.map(formDto, appointment);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                appointment.setDate(LocalDate.parse(formDto.getStartDateStr(), formatter));
                if(formDto.getEndRecurStr() != null && !formDto.getEndRecurStr().isEmpty())
                {
                    appointment.setEndRecur(LocalDate.parse(formDto.getEndRecurStr(), formatter));
                }
                else appointment.setEndRecur(null);
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
                appointment.setFrom(LocalTime.parse(formDto.getFromStr(), timeFormatter));
                appointment.setTo(LocalTime.parse(formDto.getToStr(), timeFormatter));
                appointment.setMember(member);
                appointment.setTrainer(trainerRepository.findById(formDto.getTrainerId()).orElseThrow());
                appointmentRepository.save(appointment);
            }

        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void addAppointmentInstances(AppointmentDto formDto, Member member, Appointment appointment) throws Exception
    {
        List<MemberSubscription> memberSubscriptions = memberSubscriptionRepository.findForBooking(formDto.getTrainerId(), formDto.getMemberId());
        if(memberSubscriptions.isEmpty())
        {
            throw new Exception("Người dùng không thể đăng ký");
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalDate formDate = LocalDate.parse(formDto.getStartDateStr(), formatter);
        LocalTime formFrom =  LocalTime.parse(formDto.getFromStr(), timeFormatter);
        LocalTime formTo =  LocalTime.parse(formDto.getToStr(), timeFormatter);
        LocalDate formEndRecur = null;
        if(formDto.getEndRecurStr()!= null && !formDto.getEndRecurStr().isEmpty())
        {
            formEndRecur = LocalDate.parse(formDto.getEndRecurStr(), formatter);
        }
        if(formDto.getRepeat().equals(RecurrenceType.NONE.toString()))
        {
            AppointmentInstance appointmentInstance = new AppointmentInstance();
            appointmentInstance.setDate(formDate);
            appointmentInstance.setFrom(formFrom);
            appointmentInstance.setTo(formTo);
            appointmentInstance.setStatus(1);
            appointmentInstance.setAppointment(appointment);
            appointmentInstance.setIsRepeat(0);
            appointmentInstanceRepository.save(appointmentInstance);
            for(MemberSubscription memberSubscription : memberSubscriptions)
            {
                if(memberSubscription.getNumberOfVisit() < memberSubscription.getMembership().getTotalVisit())
                {
                    memberSubscription.setNumberOfVisit(memberSubscription.getNumberOfVisit() + 1);
                    memberSubscriptionRepository.save(memberSubscription);
                    break;
                }
            }
        }
        else if(formDto.getRepeat().equals(RecurrenceType.WEEKLY.toString()))
        {
            int numberOfVisitLeft = memberRepository.findVisitsLeftForMember(member.getId(), formDto.getTrainerId()).orElseThrow();
            LocalDate date = formDate;
            int numberOfDateUse = 0;
            int curIndex = 0;
            for(int i = 0; i < numberOfVisitLeft; i++)
            {
                if(formEndRecur != null){
                    if(formEndRecur.isBefore(date))
                    {
                        break;
                    }
                }
                if(memberSubscriptions.get(memberSubscriptions.size() - 1).getEndAt().isBefore(date))
                {
                    break;
                }
                AppointmentInstance appointmentInstance = new AppointmentInstance();
                appointmentInstance.setDate(date);
                appointmentInstance.setFrom(formFrom);
                appointmentInstance.setTo(formTo);
                appointmentInstance.setStatus(1);
                appointmentInstance.setAppointment(appointment);
                appointmentInstance.setIsRepeat(1);
                appointmentInstanceRepository.save(appointmentInstance);
                date = date.plusWeeks(1);
                numberOfDateUse = numberOfDateUse + 1;
            }
            for(MemberSubscription memberSubscription : memberSubscriptions)
            {
                if(numberOfDateUse <= (memberSubscription.getMembership().getTotalVisit() - memberSubscription.getNumberOfVisit()))
                {
                    memberSubscription.setNumberOfVisit(memberSubscription.getNumberOfVisit() + numberOfDateUse);
                    numberOfDateUse = 0;
                }
                else {
                    numberOfDateUse = numberOfDateUse - (memberSubscription.getMembership().getTotalVisit() - memberSubscription.getNumberOfVisit());
                    memberSubscription.setNumberOfVisit(memberSubscription.getMembership().getTotalVisit());
                }
                memberSubscriptionRepository.save(memberSubscription);
            }
        }
    }
}
