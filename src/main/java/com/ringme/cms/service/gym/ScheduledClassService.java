package com.ringme.cms.service.gym;

import com.github.f4b6a3.uuid.UuidCreator;
import com.ringme.cms.dto.gym.CalenderEventDto;
import com.ringme.cms.dto.gym.MemberDto;
import com.ringme.cms.dto.gym.ScheduleDto;
import com.ringme.cms.enums.RecurrenceType;
import com.ringme.cms.exception.ClassInSessionException;
import com.ringme.cms.model.gym.Attendance;
import com.ringme.cms.model.gym.Member;
import com.ringme.cms.model.gym.ScheduledClass;
import com.ringme.cms.repository.gym.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Path;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

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

    public void saveClass(ScheduleDto formDto) throws Exception {
        try {
            ScheduledClass scheduledClass;
            if (formDto.getId() == null) {
                scheduledClass = modelMapper.map(formDto, ScheduledClass.class);
                scheduledClass.setStatus(1);
            } else {
                scheduledClass = scheduledClassRepository.findById(formDto.getId()).orElseThrow();
                if(hasBeenUpdated(scheduledClass, formDto))
                {
                    LocalDateTime now = LocalDateTime.now();
                    LocalDate currentDate = now.toLocalDate();
                    LocalTime currentTime = now.toLocalTime();
                    List<Attendance> attendanceList = attendanceRepository.findByScheduleIdAndDate(scheduledClass.getId(), null);
                    for(Attendance attendance : attendanceList)
                    {
                        if(attendance.getBookingTime().equals(currentDate)) {
                            if(scheduledClass.getFrom().isAfter(currentTime) || scheduledClass.getTo().isAfter(currentTime))
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
                }
                modelMapper.map(formDto, scheduledClass);
            }

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
            scheduledClassRepository.save(scheduledClass);
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            throw new RuntimeException(e);
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
}
