package com.ringme.cms.service.gym;

import com.ringme.cms.dto.gym.AppointmentDto;
import com.ringme.cms.dto.gym.CalenderEventDto;
import com.ringme.cms.dto.gym.ScheduleDto;
import com.ringme.cms.enums.RecurrenceType;
import com.ringme.cms.exception.ClassInSessionException;
import com.ringme.cms.exception.CustomExceptionWithText;
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
import java.util.Objects;

import static com.ringme.cms.enums.RecurrenceType.stringToRecurrentType;

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

    public void save(AppointmentDto formDto, String updateType) throws Exception, CustomExceptionWithText {
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
                AppointmentInstance appointmentInstance = appointmentInstanceRepository.findById(formDto.getAppointmentInstanceId()).orElseThrow();
                String originalRepeat = appointment.getRepeat().toString();
                modelMapper.map(formDto, appointment);
                LocalDateTime threshold = LocalDateTime.now().plusHours(12);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
                LocalDate formDate = LocalDate.parse(formDto.getStartDateStr(), formatter);
                LocalTime formFrom = LocalTime.parse(formDto.getFromStr(), timeFormatter);
                LocalTime formTo = LocalTime.parse(formDto.getToStr(), timeFormatter);
                if(threshold.isAfter(formDate.atTime(formFrom)))
                {
                    throw new CustomExceptionWithText("Không được sửa sau 12 tiếng!");
                }
                if(!formDate.equals(appointmentInstance.getDate()) || !formFrom.equals(appointmentInstance.getFrom()) || !formTo.equals(appointmentInstance.getTo()))
                {
                    if(updateType.equals("one"))
                    {
                        appointmentInstance.setDate(formDate);
                        appointmentInstance.setFrom(formFrom);
                        appointmentInstance.setTo(formTo);
                        appointmentInstanceRepository.save(appointmentInstance);
                    }
                    else if(updateType.equals("all")){
//                        List<AppointmentInstance> appointmentInstances = appointmentInstanceRepository.findByAppointmentIdAndDate(appointmentInstance.getAppointment().getId(), appointmentInstance.getOriginalTime());
//                        RecurrenceType recurrenceType = appointmentInstance.getAppointment().getRepeat();
//                        LocalDate date = formDate;
//                        for(AppointmentInstance item : appointmentInstances) {
//                            switch (recurrenceType) {
//                                case WEEKLY: {
//                                    date = date.plusWeeks(1);
//                                    break;
//                                }
//                                case FORTNIGHTLY: {
//                                    date = date.plusWeeks(2);
//                                    break;
//                                }
//                                case THREE_WEEKLY: {
//                                    date = date.plusWeeks(3);
//                                    break;
//                                }
//                                case FOUR_WEEKLY: {
//                                    date = date.plusWeeks(4);
//                                    break;
//                                }
//                                case SIX_WEEKLY: {
//                                    date = date.plusWeeks(6);
//                                    break;
//                                }
//                                case EIGHT_WEEKLY: {
//                                    date = date.plusWeeks(8);
//                                    break;
//                                }
//                                case MONTHLY: {
//                                    date = date.plusMonths(1);
//                                    break;
//                                }
//                                case DAILY: {
//                                    date = date.plusDays(1);
//                                    break;
//                                }
//                            }
//                            item.setDate(date);
//                            item.setFrom(formFrom);
//                            item.setTo(formTo);
//                            appointmentInstanceRepository.save(item);
//                        }
//                        appointmentInstance.setDate(formDate);
//                        appointmentInstance.setFrom(formFrom);
//                        appointmentInstance.setTo(formTo);
//                        appointmentInstanceRepository.save(appointmentInstance);
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
                        addAppointmentInstances(formDto, member, appointment);
                    }
                    else {
//                        List<AppointmentInstance> appointmentInstances = appointmentInstanceRepository.findByAppointmentIdAndThreshold(appointmentInstance.getAppointment().getId(), threshold.toLocalDate(), threshold.toLocalTime());
//                        for(AppointmentInstance item : appointmentInstances) {
//                            item.setFrom(formFrom);
//                            item.setTo(formTo);
//                            appointmentInstanceRepository.save(item);
//                        }
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
                        formDto.setStartDateStr(startDate.format(formatter));
                        addAppointmentInstances(formDto, member, appointment);
                    }
                }
                else {
                    appointmentInstance.setDate(formDate);
                    appointmentInstance.setFrom(formFrom);
                    appointmentInstance.setTo(formTo);
                    appointmentInstanceRepository.save(appointmentInstance);
                }
                LocalDate originalEndRecur = appointment.getEndRecur();
                if(formDto.getEndRecurStr() != null && !formDto.getEndRecurStr().isEmpty())
                {
                    appointment.setEndRecur(LocalDate.parse(formDto.getEndRecurStr(), formatter));
                }
                else appointment.setEndRecur(null);
                appointment.setMember(member);
                appointment.setTrainer(trainerRepository.findById(formDto.getTrainerId()).orElseThrow());
                appointmentRepository.save(appointment);
                if(!Objects.equals(originalEndRecur, appointment.getEndRecur()) && !originalRepeat.equals("NONE"))
                {
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
                    formDto.setStartDateStr(startDate.format(formatter));
                    addAppointmentInstances(formDto, member, appointment);
                }
                if(!formDto.getRepeat().equals(originalRepeat))
                {
                    if(formDto.getRepeat().equals(RecurrenceType.NONE.toString()))
                    {
                        List<MemberSubscription> memberSubscriptions = memberSubscriptionRepository.findForBooking(appointment.getTrainer().getId(), appointment.getMember().getId());
                        List<AppointmentInstance> appointmentInstances = appointmentInstanceRepository.findByAppointmentIdAndThreshold(appointmentInstance.getAppointment().getId(), threshold.toLocalDate(), threshold.toLocalTime());
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
                        addAppointmentInstances(formDto, member, appointment);
                        appointment.setEndRecur(null);
                        appointmentRepository.save(appointment);
                    }
                    else if(updateType.equals("every"))
                    {
                        List<MemberSubscription> memberSubscriptions = memberSubscriptionRepository.findForBooking(appointment.getTrainer().getId(), appointment.getMember().getId());
                        List<AppointmentInstance> appointmentInstances = appointmentInstanceRepository.findByAppointmentIdAndThreshold(appointmentInstance.getAppointment().getId(), threshold.toLocalDate(), threshold.toLocalTime());
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
                        appointment.setDate(appointmentInstance.getDate());
                        appointmentRepository.save(appointment);
                        addAppointmentInstances(formDto, member, appointment);
                    }
                    else if(updateType.equals("all"))
                    {
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
                        appointment.setRepeat(stringToRecurrentType(originalRepeat));
                        appointmentRepository.save(appointment);
                        Appointment newAppointment;
                        newAppointment = modelMapper.map(formDto, Appointment.class);
                        newAppointment.setId(null);
                        newAppointment.setStatus(1);
                        newAppointment.setDate(LocalDate.parse(formDto.getStartDateStr(), formatter));
                        if(formDto.getEndRecurStr() != null && !formDto.getEndRecurStr().isEmpty())
                        {
                            newAppointment.setEndRecur(LocalDate.parse(formDto.getEndRecurStr(), formatter));
                        }
                        else newAppointment.setEndRecur(null);
                        newAppointment.setFrom(LocalTime.parse(formDto.getFromStr(), timeFormatter));
                        newAppointment.setTo(LocalTime.parse(formDto.getToStr(), timeFormatter));
                        newAppointment.setMember(member);
                        newAppointment.setTrainer(trainerRepository.findById(formDto.getTrainerId()).orElseThrow());
                        Appointment savedAppointment = appointmentRepository.save(newAppointment);
                        addAppointmentInstances(formDto, member, savedAppointment);
                    }
                }
            }
    }

    public void addAppointmentInstances(AppointmentDto formDto, Member member, Appointment appointment) throws Exception
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
            appointmentInstance.setOriginalTime(formDate.atTime(formFrom));
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
        else
        {
            addRecurrentInstance(formDto.getRepeat(), formDate, formFrom, formTo, formEndRecur, memberSubscriptions, member, formDto, appointment);
        }
    }

    private void addRecurrentInstance(String recurrenceType, LocalDate formDate,
                                      LocalTime formFrom, LocalTime formTo,LocalDate formEndRecur,
                                      List<MemberSubscription> memberSubscriptions, Member member,
                                      AppointmentDto formDto, Appointment appointment ) throws Exception
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
            appointmentInstance.setOriginalTime(date.atTime(formFrom));
            appointmentInstanceRepository.save(appointmentInstance);
            if(Objects.equals(recurrenceType, RecurrenceType.WEEKLY.toString()))
            {
                date = date.plusWeeks(1);
            }
            else if(Objects.equals(recurrenceType, RecurrenceType.FORTNIGHTLY.toString()))
            {
                date = date.plusWeeks(2);
            }
            else if(Objects.equals(recurrenceType, RecurrenceType.THREE_WEEKLY.toString()))
            {
                date = date.plusWeeks(3);
            }
            else if(Objects.equals(recurrenceType, RecurrenceType.FOUR_WEEKLY.toString()))
            {
                date = date.plusWeeks(4);
            }
            else if(Objects.equals(recurrenceType, RecurrenceType.SIX_WEEKLY.toString()))
            {
                date = date.plusWeeks(6);
            }
            else if(Objects.equals(recurrenceType, RecurrenceType.EIGHT_WEEKLY.toString()))
            {
                date = date.plusWeeks(8);
            }
            else if(Objects.equals(recurrenceType, RecurrenceType.MONTHLY.toString()))
            {
                date = date.plusMonths(1);
            }
            else if(Objects.equals(recurrenceType, RecurrenceType.DAILY.toString()))
            {
                date = date.plusDays(1);
            }
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

    public void delete(Long id, String type) throws Exception
    {
        AppointmentInstance appointmentInstance = appointmentInstanceRepository.findById(id).orElseThrow();
        Appointment appointment = appointmentInstance.getAppointment();
        List<MemberSubscription> memberSubscriptions = memberSubscriptionRepository.findForBooking(appointment.getTrainer().getId(), appointment.getMember().getId());
        LocalDateTime threshold = LocalDateTime.now().plusHours(12);
        log.info(threshold + "," + appointmentInstance.getDate().atTime(appointmentInstance.getFrom()));
        if(threshold.isAfter(appointmentInstance.getDate().atTime(appointmentInstance.getFrom())))
        {
            throw new ClassInSessionException("Không thể xóa trước 12 tiếng");
        }
        if(type.equals("one"))
        {
            appointmentInstance.setStatus(0);
            appointmentInstanceRepository.save(appointmentInstance);
            for(MemberSubscription memberSubscription : memberSubscriptions)
            {
                if(memberSubscription.getNumberOfVisit() != 0)
                {
                    memberSubscription.setNumberOfVisit(memberSubscription.getNumberOfVisit() - 1);
                    memberSubscriptionRepository.save(memberSubscription);
                    break;
                }
            }
        }
        else if(type.equals("all"))
        {
            List<AppointmentInstance> appointmentInstances = appointmentInstanceRepository.findByAppointmentIdAndDate(appointmentInstance.getAppointment().getId(), appointmentInstance.getOriginalTime());
            int count = 1;
            for(AppointmentInstance item : appointmentInstances)
            {
                item.setStatus(0);
                appointmentInstanceRepository.save(item);
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
            appointmentInstance.setStatus(0);
            appointmentInstanceRepository.save(appointmentInstance);
        }
        else {
            List<AppointmentInstance> appointmentInstances = appointmentInstanceRepository.findByAppointmentIdAndThreshold(appointmentInstance.getAppointment().getId(), threshold.toLocalDate(), threshold.toLocalTime());
            int count = 0;
            for(AppointmentInstance item : appointmentInstances)
            {
                item.setStatus(0);
                appointmentInstanceRepository.save(item);
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
        }
    }
}
