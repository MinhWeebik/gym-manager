package com.ringme.cms.service.gym;

import com.github.f4b6a3.uuid.UuidCreator;
import com.ringme.cms.dto.gym.AppointmentDto;
import com.ringme.cms.dto.gym.CalenderEventDto;
import com.ringme.cms.dto.gym.MemberDto;
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
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.file.Path;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.ringme.cms.enums.RecurrenceType.stringToRecurrentType;

@Service
@Log4j2
@RequiredArgsConstructor
public class ScheduledClassService {

    private final ScheduledClassRepository scheduledClassRepository;

    private final ModelMapper modelMapper;

    private final ClassRepository classRepository;

    private final TrainerRepository trainerRepository;

    private final AttendanceRepository attendanceRepository;
    private final MemberRepository memberRepository;
    private final ScheduledClassInstanceRepository scheduledClassInstanceRepository;

    public List<ScheduledClass> getAllScheduledClasses(LocalDate start, LocalDate end, Long trainerId) throws Exception{
        return  scheduledClassRepository.getAllNoneRepeat(start, end, trainerId);
    }

    public List<ScheduledClass> getAllRepeatedSchedule(LocalDate start, LocalDate end, Long trainerId) throws Exception{
        return scheduledClassRepository.getAllRepeat(start, end, trainerId);
    }

    public void updateScheduledClass(Long id, CalenderEventDto dto) throws Exception
    {
        ScheduledClass scheduledClass = scheduledClassRepository.findById(id).orElseThrow();
        LocalDate updatedDate = dto.getStartTime().toLocalDate();
        if(updatedDate != scheduledClass.getDate() || dto.getStartTime().toLocalTime() != scheduledClass.getFrom() || dto.getEndTime().toLocalTime() != scheduledClass.getTo())
        {
            LocalDateTime now = LocalDateTime.now();
            LocalDate currentDate = now.toLocalDate();
            LocalTime currentTime = now.toLocalTime();
            if(scheduledClass.getRepeat() != RecurrenceType.NONE)
            {
                List<Attendance> attendanceList = attendanceRepository.findByScheduleIdAndDateLimit(id, currentDate);
                for (Attendance attendance : attendanceList)
                {
                    if(attendance.getBookingTime().equals(currentDate))
                    {
                        if(scheduledClass.getFrom().isAfter(currentTime) || scheduledClass.getTo().isAfter(currentTime))
                        {
                            attendance.setStatus(0);
                            attendanceRepository.save(attendance);
                            Member member = memberRepository.findById(attendance.getMember().getId()).orElseThrow();
                            member.setCoin(member.getCoin()+scheduledClass.getPrice());
                            memberRepository.save(member);
                        }
                        else if(dto.getStartTime().toLocalTime().isAfter(currentTime) || dto.getEndTime().toLocalTime().isAfter(currentTime))
                        {
                            attendance.setStatus(0);
                            attendanceRepository.save(attendance);
                            Member member = memberRepository.findById(attendance.getMember().getId()).orElseThrow();
                            member.setCoin(member.getCoin()+scheduledClass.getPrice());
                            memberRepository.save(member);
                        }
                    }
                    else if(attendance.getBookingTime().isAfter(currentDate)) {
                        attendance.setStatus(0);
                        attendanceRepository.save(attendance);
                        Member member = memberRepository.findById(attendance.getMember().getId()).orElseThrow();
                        member.setCoin(member.getCoin()+scheduledClass.getPrice());
                        memberRepository.save(member);
                    }
                }
                if(scheduledClass.getFrom().equals(dto.getStartTime().toLocalTime()) && scheduledClass.getTo().equals(dto.getEndTime().toLocalTime()))
                {
                    scheduledClass.setDate(updatedDate);
                }
                else {
                    scheduledClass.setFrom(dto.getStartTime().toLocalTime());
                    scheduledClass.setTo(dto.getEndTime().toLocalTime());
                }
            }
            else {


//                if (scheduledClass.getDate().equals(currentDate) &&
//                        (scheduledClass.getFrom().isBefore(currentTime) || scheduledClass.getFrom().equals(currentTime)) &&
//                        (scheduledClass.getTo().isAfter(currentTime)  || scheduledClass.getTo().equals(currentTime)))
//                {
//                    throw new ClassInSessionException("Lớp đang trong tiến trình, không thể thay đổi");
//                }

                if(scheduledClass.getDate().equals(currentDate))
                {
                    if(scheduledClass.getFrom().isAfter(currentTime) || scheduledClass.getTo().isAfter(currentTime))
                    {
                        List<Attendance> attendanceList = attendanceRepository.findByScheduleIdAndDate(id, scheduledClass.getDate());
                        for (Attendance attendance : attendanceList)
                        {
                            attendance.setStatus(0);
                            attendanceRepository.save(attendance);
                            Member member = memberRepository.findById(attendance.getMember().getId()).orElseThrow();
                            member.setCoin(member.getCoin()+scheduledClass.getPrice());
                            memberRepository.save(member);
                        }
                    }
                    else if(dto.getStartTime().toLocalTime().isAfter(currentTime) || dto.getEndTime().toLocalTime().isAfter(currentTime))
                    {
                        List<Attendance> attendanceList = attendanceRepository.findByScheduleIdAndDate(id, scheduledClass.getDate());
                        for (Attendance attendance : attendanceList)
                        {
                            attendance.setStatus(0);
                            attendanceRepository.save(attendance);
                            Member member = memberRepository.findById(attendance.getMember().getId()).orElseThrow();
                            member.setCoin(member.getCoin()+scheduledClass.getPrice());
                            memberRepository.save(member);
                        }
                    }
                }
                else if(scheduledClass.getDate().isAfter(currentDate)){
                    List<Attendance> attendanceList = attendanceRepository.findByScheduleIdAndDate(id, scheduledClass.getDate());
                    for (Attendance attendance : attendanceList)
                    {
                        attendance.setStatus(0);
                        attendanceRepository.save(attendance);
                        Member member = memberRepository.findById(attendance.getMember().getId()).orElseThrow();
                        member.setCoin(member.getCoin()+scheduledClass.getPrice());
                        memberRepository.save(member);
                    }
                }

                scheduledClass.setDate(updatedDate);
                scheduledClass.setFrom(dto.getStartTime().toLocalTime());
                scheduledClass.setTo(dto.getEndTime().toLocalTime());
            }
            scheduledClassRepository.save(scheduledClass);
        }
    }

    @Transactional
    public void saveClass(ScheduleDto formDto, String updateType) throws Exception {
//        try {
//            ScheduledClass scheduledClass;
//            if (formDto.getId() == null) {
//                scheduledClass = modelMapper.map(formDto, ScheduledClass.class);
//                scheduledClass.setStatus(1);
//            } else {
//                scheduledClass = scheduledClassRepository.findById(formDto.getId()).orElseThrow();
//                if(hasBeenUpdated(scheduledClass, formDto))
//                {
//                    LocalDateTime now = LocalDateTime.now();
//                    LocalDate currentDate = now.toLocalDate();
//                    LocalTime currentTime = now.toLocalTime();
//                    List<Attendance> attendanceList = attendanceRepository.findByScheduleIdAndDate(scheduledClass.getId(), null);
//                    for(Attendance attendance : attendanceList)
//                    {
//                        if(attendance.getBookingTime().equals(currentDate)) {
//                            if(scheduledClass.getFrom().isAfter(currentTime) || scheduledClass.getTo().isAfter(currentTime))
//                            {
//                                attendance.setStatus(0);
//                                attendanceRepository.save(attendance);
//                                Member member = memberRepository.findById(attendance.getMember().getId()).orElseThrow();
//                                member.setCoin(member.getCoin()+scheduledClass.getPrice());
//                                memberRepository.save(member);
//                            }
//                        }
//                        else if(attendance.getBookingTime().isAfter(currentDate)) {
//                            attendance.setStatus(0);
//                            attendanceRepository.save(attendance);
//                            Member member = memberRepository.findById(attendance.getMember().getId()).orElseThrow();
//                            member.setCoin(member.getCoin()+scheduledClass.getPrice());
//                            memberRepository.save(member);
//                        }
//                    }
//                }
//                modelMapper.map(formDto, scheduledClass);
//            }
//
//            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
//            scheduledClass.setDate(LocalDate.parse(formDto.getStartDateStr(), formatter));
//            if(formDto.getEndRecurStr() != null && !formDto.getEndRecurStr().isEmpty())
//            {
//                scheduledClass.setEndRecur(LocalDate.parse(formDto.getEndRecurStr(), formatter));
//            }
//            else scheduledClass.setEndRecur(null);
//            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
//            scheduledClass.setFrom(LocalTime.parse(formDto.getFromStr(), timeFormatter));
//            scheduledClass.setTo(LocalTime.parse(formDto.getToStr(), timeFormatter));
//            scheduledClass.setClasses(classRepository.findById(formDto.getClassId()).orElseThrow());
//            scheduledClass.setTrainer(trainerRepository.findById(formDto.getTrainerId()).orElseThrow());
//            scheduledClassRepository.save(scheduledClass);
//        } catch (Exception e) {
//            log.error("Exception: {}", e.getMessage(), e);
//            throw new RuntimeException(e);
//        }

        ScheduledClass scheduledClass;
        if (formDto.getId() == null) {
            scheduledClass = modelMapper.map(formDto, ScheduledClass.class);
            scheduledClass.setStatus(1);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            scheduledClass.setDate(LocalDate.parse(formDto.getStartDateStr(), formatter));
            if(formDto.getEndRecurStr() != null && !formDto.getEndRecurStr().isEmpty())
            {
                scheduledClass.setEndRecur(LocalDate.parse(formDto.getEndRecurStr(), formatter));
            }
            else scheduledClass.setEndRecur(null);
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            scheduledClass.setFrom(LocalTime.parse(formDto.getFromStr(), timeFormatter));
            scheduledClass.setTo(LocalTime.parse(formDto.getToStr(), timeFormatter));
            scheduledClass.setClasses(classRepository.findById(formDto.getClassId()).orElseThrow());
            scheduledClass.setTrainer(trainerRepository.findById(formDto.getTrainerId()).orElseThrow());
            ScheduledClass savedScheduledClass = scheduledClassRepository.save(scheduledClass);
            addScheduledClassInstance(formDto, savedScheduledClass);
        }
        else {
            ScheduledClassInstance scheduledClassInstance = scheduledClassInstanceRepository.findById(formDto.getId()).orElseThrow();
            scheduledClass = scheduledClassInstance.getScheduledClass();
            String originalRepeat = scheduledClass.getRepeat().toString();
            scheduledClass.setBackgroundColor(formDto.getBackgroundColor());
            scheduledClass.setNote(formDto.getNote());
            scheduledClass.setRepeat(RecurrenceType.valueOf(formDto.getRepeat()));
            LocalDateTime threshold = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            LocalDate formDate = LocalDate.parse(formDto.getStartDateStr(), formatter);
            LocalTime formFrom = LocalTime.parse(formDto.getFromStr(), timeFormatter);
            LocalTime formTo = LocalTime.parse(formDto.getToStr(), timeFormatter);
            if(threshold.isAfter(formDate.atTime(formFrom)))
            {
                throw new CustomExceptionWithText("Không được sửa!");
            }
            if(!formDate.equals(scheduledClassInstance.getDate()) || !formFrom.equals(scheduledClassInstance.getFrom()) || !formTo.equals(scheduledClassInstance.getTo())) {
                if (updateType.equals("one")) {
                    scheduledClassInstance.setDate(formDate);
                    scheduledClassInstance.setFrom(formFrom);
                    scheduledClassInstance.setTo(formTo);
                    scheduledClassInstanceRepository.save(scheduledClassInstance);
                    memberRepository.refundCoin(List.of(formDto.getId()));
                    attendanceRepository.bulkDelete(List.of(formDto.getId()));
                }
                else if (updateType.equals("all"))
                {
                    List<Long> ids = scheduledClassInstanceRepository.findIdByAppointmentIdAndDate(scheduledClassInstance.getScheduledClass().getId(), scheduledClassInstance.getOriginalTime());
                    memberRepository.refundCoin(ids);
                    attendanceRepository.bulkDelete(ids);
                    scheduledClassInstanceRepository.bulkDeleteHard(ids);
                    addScheduledClassInstance(formDto, scheduledClass);
                }
                else {
                    List<Long> ids = scheduledClassInstanceRepository.findIdByScheduleIdAndThreshold(scheduledClassInstance.getScheduledClass().getId(), threshold.toLocalDate(), threshold.toLocalTime());
                    ScheduledClassInstance sci = scheduledClassInstanceRepository.findById(ids.get(0)).orElseThrow();
                    memberRepository.refundCoin(ids);
                    attendanceRepository.bulkDelete(ids);
                    scheduledClassInstanceRepository.bulkDeleteHard(ids);
                    formDto.setStartDateStr(sci.getDate().format(formatter));
                    addScheduledClassInstance(formDto, scheduledClass);
                }
            }
            else {
                scheduledClassInstance.setDate(formDate);
                scheduledClassInstance.setFrom(formFrom);
                scheduledClassInstance.setTo(formTo);
                scheduledClassInstanceRepository.save(scheduledClassInstance);
            }
            LocalDate originalEndRecur = scheduledClass.getEndRecur();
            if(formDto.getEndRecurStr() != null && !formDto.getEndRecurStr().isEmpty())
            {
                scheduledClass.setEndRecur(LocalDate.parse(formDto.getEndRecurStr(), formatter));
            }
            else scheduledClass.setEndRecur(null);
            scheduledClassRepository.save(scheduledClass);
            if(!Objects.equals(originalEndRecur, scheduledClass.getEndRecur()) && !originalRepeat.equals("NONE"))
            {
                List<Long> ids = scheduledClassInstanceRepository.findIdByScheduleIdAndThreshold(scheduledClassInstance.getScheduledClass().getId(), threshold.toLocalDate(), threshold.toLocalTime());
                ScheduledClassInstance sci = scheduledClassInstanceRepository.findById(ids.get(0)).orElseThrow();
                memberRepository.refundCoin(ids);
                attendanceRepository.bulkDelete(ids);
                scheduledClassInstanceRepository.bulkDeleteHard(ids);
                formDto.setStartDateStr(sci.getDate().format(formatter));
                addScheduledClassInstance(formDto, scheduledClass);
            }
            if(!formDto.getRepeat().equals(originalRepeat)) {
                if (formDto.getRepeat().equals(RecurrenceType.NONE.toString())) {
                    List<Long> ids = scheduledClassInstanceRepository.findIdByScheduleIdAndThreshold(scheduledClassInstance.getScheduledClass().getId(), threshold.toLocalDate(), threshold.toLocalTime());
                    memberRepository.refundCoin(ids);
                    attendanceRepository.bulkDelete(ids);
                    scheduledClassInstanceRepository.bulkDeleteHard(ids);
                    addScheduledClassInstance(formDto, scheduledClass);
                }
                else if(updateType.equals("all"))
                {
                    List<Long> ids = scheduledClassInstanceRepository.findIdByAppointmentIdAndDate(scheduledClassInstance.getScheduledClass().getId(), scheduledClassInstance.getOriginalTime());
                    memberRepository.refundCoin(ids);
                    attendanceRepository.bulkDelete(ids);
                    scheduledClassInstanceRepository.bulkDeleteHard(ids);
                    scheduledClass.setRepeat(stringToRecurrentType(originalRepeat));
                    scheduledClassRepository.save(scheduledClass);
                    ScheduledClass newScheduledClass;
                    newScheduledClass = modelMapper.map(formDto, ScheduledClass.class);
                    newScheduledClass.setId(null);
                    newScheduledClass.setStatus(1);
                    newScheduledClass.setDate(LocalDate.parse(formDto.getStartDateStr(), formatter));
                    if(formDto.getEndRecurStr() != null && !formDto.getEndRecurStr().isEmpty())
                    {
                        newScheduledClass.setEndRecur(LocalDate.parse(formDto.getEndRecurStr(), formatter));
                    }
                    else newScheduledClass.setEndRecur(null);
                    newScheduledClass.setFrom(LocalTime.parse(formDto.getFromStr(), timeFormatter));
                    newScheduledClass.setTo(LocalTime.parse(formDto.getToStr(), timeFormatter));
                    newScheduledClass.setClasses(scheduledClass.getClasses());
                    newScheduledClass.setTrainer(trainerRepository.findById(formDto.getTrainerId()).orElseThrow());
                    ScheduledClass savedScheduledClass = scheduledClassRepository.save(newScheduledClass);
                    addScheduledClassInstance(formDto, savedScheduledClass);
                }
                else if(updateType.equals("every"))
                {
                    List<Long> ids = scheduledClassInstanceRepository.findIdByScheduleIdAndThreshold(scheduledClassInstance.getScheduledClass().getId(), threshold.toLocalDate(), threshold.toLocalTime());
                    ScheduledClassInstance sci = scheduledClassInstanceRepository.findById(ids.get(0)).orElseThrow();
                    formDto.setStartDateStr(sci.getDate().format(formatter));
                    memberRepository.refundCoin(ids);
                    attendanceRepository.bulkDelete(ids);
                    scheduledClassInstanceRepository.bulkDeleteHard(ids);
                    scheduledClass.setRepeat(stringToRecurrentType(originalRepeat));
                    scheduledClassRepository.save(scheduledClass);
                    ScheduledClass newScheduledClass;
                    newScheduledClass = modelMapper.map(formDto, ScheduledClass.class);
                    newScheduledClass.setId(null);
                    newScheduledClass.setStatus(1);
                    newScheduledClass.setDate(LocalDate.parse(formDto.getStartDateStr(), formatter));
                    if(formDto.getEndRecurStr() != null && !formDto.getEndRecurStr().isEmpty())
                    {
                        newScheduledClass.setEndRecur(LocalDate.parse(formDto.getEndRecurStr(), formatter));
                    }
                    else newScheduledClass.setEndRecur(null);
                    newScheduledClass.setFrom(LocalTime.parse(formDto.getFromStr(), timeFormatter));
                    newScheduledClass.setTo(LocalTime.parse(formDto.getToStr(), timeFormatter));
                    newScheduledClass.setClasses(scheduledClass.getClasses());
                    newScheduledClass.setTrainer(trainerRepository.findById(formDto.getTrainerId()).orElseThrow());
                    ScheduledClass savedScheduledClass = scheduledClassRepository.save(newScheduledClass);
                    addScheduledClassInstance(formDto, savedScheduledClass);
                }
            }
            if(!Objects.equals(formDto.getTrainerId(), scheduledClassInstance.getTrainer().getId()) || !Objects.equals(formDto.getPrice(), scheduledClassInstance.getPrice()) || !Objects.equals(formDto.getCapacity(), scheduledClassInstance.getCapacity()))
            {
                if(updateType.equals("one"))
                {
                    if(!Objects.equals(formDto.getTrainerId(), scheduledClassInstance.getTrainer().getId()))
                    {
                        scheduledClassInstance.setTrainer(trainerRepository.findById(formDto.getTrainerId()).orElseThrow());
                        scheduledClassInstanceRepository.save(scheduledClassInstance);
                    }
                    else if(!Objects.equals(formDto.getPrice(), scheduledClassInstance.getPrice()))
                    {
                        if(formDto.getPrice() < scheduledClassInstance.getPrice())
                        {
                            memberRepository.bulkUpdateCoin(List.of(scheduledClassInstance.getId()), scheduledClassInstance.getPrice() - formDto.getPrice());
                        }
                        scheduledClassInstance.setPrice(formDto.getPrice());
                        scheduledClassInstanceRepository.save(scheduledClassInstance);
                    }
                    else if(!Objects.equals(formDto.getCapacity(), scheduledClassInstance.getCapacity()))
                    {
                        scheduledClassInstance.setCapacity(formDto.getCapacity());
                        scheduledClassInstanceRepository.save(scheduledClassInstance);
                        if(attendanceRepository.getNumberOfAttendance(scheduledClassInstance.getId()) > scheduledClassInstance.getCapacity())
                        {
                            throw new RuntimeException();
                        }
                    }
                }
                else if(updateType.equals("all"))
                {
                    if(!Objects.equals(formDto.getTrainerId(), scheduledClassInstance.getTrainer().getId()))
                    {
                        List<Long> ids = scheduledClassInstanceRepository.findIdByAppointmentIdAndDate(scheduledClassInstance.getScheduledClass().getId(), scheduledClassInstance.getOriginalTime());
                        scheduledClassInstanceRepository.bulkUpdateTrainer(ids, formDto.getTrainerId());
                    }
                    else if(!Objects.equals(formDto.getPrice(), scheduledClassInstance.getPrice()))
                    {
                        List<Long> ids = scheduledClassInstanceRepository.findIdByAppointmentIdAndDate(scheduledClassInstance.getScheduledClass().getId(), scheduledClassInstance.getOriginalTime());
                        for(Long id : ids)
                        {
                            ScheduledClassInstance sci = scheduledClassInstanceRepository.findById(id).orElseThrow();
                            if(formDto.getPrice() < sci.getPrice())
                            {
                                memberRepository.bulkUpdateCoin(List.of(id), sci.getPrice() - formDto.getPrice());
                            }
                        }
                        scheduledClassInstanceRepository.bulkUpdatePrice(ids, formDto.getPrice());
                    }
                    else if(!Objects.equals(formDto.getCapacity(), scheduledClassInstance.getCapacity()))
                    {
                        List<Long> ids = scheduledClassInstanceRepository.findIdByAppointmentIdAndDate(scheduledClassInstance.getScheduledClass().getId(), scheduledClassInstance.getOriginalTime());
                        scheduledClassInstanceRepository.bulkUpdateCapacity(ids, formDto.getCapacity());
                        for(Long id : ids)
                        {
                            if(attendanceRepository.getNumberOfAttendance(id) > formDto.getCapacity())
                            {
                                throw new RuntimeException();
                            }
                        }
                    }
                }
                else {
                    if(!Objects.equals(formDto.getTrainerId(), scheduledClassInstance.getTrainer().getId()))
                    {
                        List<Long> ids = scheduledClassInstanceRepository.findIdByScheduleIdAndThreshold(scheduledClassInstance.getScheduledClass().getId(), threshold.toLocalDate(), threshold.toLocalTime());
                        scheduledClassInstanceRepository.bulkUpdateTrainer(ids, formDto.getTrainerId());
                    }
                    else if(!Objects.equals(formDto.getPrice(), scheduledClassInstance.getPrice()))
                    {
                        List<Long> ids = scheduledClassInstanceRepository.findIdByScheduleIdAndThreshold(scheduledClassInstance.getScheduledClass().getId(), threshold.toLocalDate(), threshold.toLocalTime());
                        for(Long id : ids)
                        {
                            ScheduledClassInstance sci = scheduledClassInstanceRepository.findById(id).orElseThrow();
                            if(formDto.getPrice() < sci.getPrice())
                            {
                                memberRepository.bulkUpdateCoin(List.of(id), sci.getPrice() - formDto.getPrice());
                            }
                        }
                        scheduledClassInstanceRepository.bulkUpdatePrice(ids, formDto.getPrice());
                    }
                    else if(!Objects.equals(formDto.getCapacity(), scheduledClassInstance.getCapacity()))
                    {
                        List<Long> ids = scheduledClassInstanceRepository.findIdByScheduleIdAndThreshold(scheduledClassInstance.getScheduledClass().getId(), threshold.toLocalDate(), threshold.toLocalTime());
                        scheduledClassInstanceRepository.bulkUpdateCapacity(ids, formDto.getCapacity());
                        for(Long id : ids)
                        {
                            if(attendanceRepository.getNumberOfAttendance(id) > formDto.getCapacity())
                            {
                                throw new RuntimeException();
                            }
                        }
                    }
                }
            }
        }
    }

    public void addScheduledClassInstance(ScheduleDto formDto, ScheduledClass scheduledClass) throws Exception
    {
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
            ScheduledClassInstance scheduledClassInstance = new ScheduledClassInstance();
            scheduledClassInstance.setDate(formDate);
            scheduledClassInstance.setFrom(formFrom);
            scheduledClassInstance.setTo(formTo);
            scheduledClassInstance.setStatus(1);
            scheduledClassInstance.setScheduledClass(scheduledClass);
            scheduledClassInstance.setIsRepeat(0);
            scheduledClassInstance.setPrice(formDto.getPrice());
            scheduledClassInstance.setCapacity(formDto.getCapacity());
            scheduledClassInstance.setOriginalTime(formDate.atTime(formFrom));
            scheduledClassInstance.setTrainer(trainerRepository.findById(formDto.getTrainerId()).orElseThrow());
            scheduledClassInstanceRepository.save(scheduledClassInstance);
        }
        else
        {
            addRecurrentInstance(formDto.getRepeat(), formDate, formFrom, formTo, formEndRecur, formDto, scheduledClass);
        }
    }

    private void addRecurrentInstance(String recurrenceType, LocalDate formDate,
                                      LocalTime formFrom, LocalTime formTo,LocalDate formEndRecur,
                                      ScheduleDto formDto, ScheduledClass scheduledClass ) throws Exception
    {
        int numberOfVisitLeft = 50;
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
            ScheduledClassInstance scheduledClassInstance = new ScheduledClassInstance();
            scheduledClassInstance.setDate(date);
            scheduledClassInstance.setFrom(formFrom);
            scheduledClassInstance.setTo(formTo);
            scheduledClassInstance.setStatus(1);
            scheduledClassInstance.setScheduledClass(scheduledClass);
            scheduledClassInstance.setIsRepeat(1);
            scheduledClassInstance.setPrice(formDto.getPrice());
            scheduledClassInstance.setCapacity(formDto.getCapacity());
            scheduledClassInstance.setOriginalTime(date.atTime(formFrom));
            scheduledClassInstance.setTrainer(trainerRepository.findById(formDto.getTrainerId()).orElseThrow());
            scheduledClassInstanceRepository.save(scheduledClassInstance);
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
    }

    private boolean hasBeenUpdated(ScheduledClass scheduledClass, ScheduleDto formDto) {
        LocalDate formStartDate = LocalDate.parse(formDto.getStartDateStr(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        LocalTime formFromTime = LocalTime.parse(formDto.getFromStr(), DateTimeFormatter.ofPattern("HH:mm"));
        LocalTime formToTime = LocalTime.parse(formDto.getToStr(), DateTimeFormatter.ofPattern("HH:mm"));
        if (!Objects.equals(scheduledClass.getClasses().getId(), formDto.getClassId())) {
            return true;
        }
        if (!Objects.equals(scheduledClass.getTrainer().getId(), formDto.getTrainerId())) {
            return true;
        }
        if (!scheduledClass.getDate().equals(formStartDate)) {
            return true;
        }
        if (!scheduledClass.getFrom().equals(formFromTime)) {
            return true;
        }
        if (!scheduledClass.getTo().equals(formToTime)) {
            return true;
        }
        if (!scheduledClass.getPrice().equals(formDto.getPrice())) {
            return true;
        }
        if (!scheduledClass.getRepeat().toString().equals(formDto.getRepeat())) {
            return true;
        }

        LocalDate formEndRecur = null;
        if (formDto.getEndRecurStr() != null && !formDto.getEndRecurStr().isEmpty()) {
            formEndRecur = LocalDate.parse(formDto.getEndRecurStr(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }

        return !Objects.equals(scheduledClass.getEndRecur(), formEndRecur);
    }

    @Transactional
    public void delete(Long id, String type) throws Exception
    {
        ScheduledClassInstance scheduledClassInstance = scheduledClassInstanceRepository.findById(id).orElseThrow();
        ScheduledClass scheduledClass = scheduledClassInstance.getScheduledClass();
        LocalDateTime threshold = LocalDateTime.now();
        if(threshold.isAfter(scheduledClassInstance.getDate().atTime(scheduledClassInstance.getFrom())))
        {
            throw new ClassInSessionException("Không thể hủy lớp");
        }
        if(type.equals("one"))
        {
            memberRepository.refundCoin(List.of(id));
            scheduledClassInstance.setStatus(0);
            scheduledClassInstanceRepository.save(scheduledClassInstance);
            attendanceRepository.bulkDelete(List.of(id));
        }
        else if(type.equals("all"))
        {
            List<Long> ids = scheduledClassInstanceRepository.findIdByAppointmentIdAndDate(scheduledClassInstance.getScheduledClass().getId(), scheduledClassInstance.getOriginalTime());
            memberRepository.refundCoin(ids);
            scheduledClassInstanceRepository.bulkDelete(ids);
            attendanceRepository.bulkDelete(ids);
        }
        else {
            List<Long> ids = scheduledClassInstanceRepository.findIdByScheduleIdAndThreshold(scheduledClassInstance.getScheduledClass().getId(), threshold.toLocalDate(), threshold.toLocalTime());
            memberRepository.refundCoin(ids);
            scheduledClassInstanceRepository.bulkDelete(ids);
            attendanceRepository.bulkDelete(ids);
        }
    }
}
