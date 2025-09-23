package com.ringme.cms.controller.gym;

import com.ringme.cms.dto.ValueTextDto;
import com.ringme.cms.dto.gym.*;
import com.ringme.cms.enums.RecurrenceType;
import com.ringme.cms.exception.ClassInSessionException;
import com.ringme.cms.exception.CustomExceptionWithText;
import com.ringme.cms.model.gym.*;
import com.ringme.cms.repository.gym.*;
import com.ringme.cms.service.gym.*;
import com.ringme.cms.utils.AppUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@Log4j2
@RequestMapping("/schedule")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduledClassService scheduledClassService;
    private final ScheduledClassRepository scheduledClassRepository;
    private final TrainerRepository trainerRepository;
    private final ClassRepository classRepository;
    private final ModelMapper modelMapper;
    private final BlockedTimeService blockedTimeService;
    private final BlockedTimeRepository blockedTimeRepository;
    private final AttendanceRepository attendanceRepository;
    private final AttendanceService attendanceService;
    private final MemberRepository memberRepository;
    private final AppointmentService appointmentService;
    private final AppointmentRepository appointmentRepository;
    private final AppointmentInstanceService appointmentInstanceService;
    private final AppointmentInstanceRepository appointmentInstanceRepository;

    @RequestMapping(value = "/index")
    private String index() {
        return "gym/schedule/index";
    }

    @GetMapping(value = "/calender-data")
    @ResponseBody
    private List<CalenderEventDto> calenderData(@RequestParam(name = "trainerId", required = false) Long trainerId,
                                                @RequestParam(name = "start") String start,
                                                @RequestParam(name = "end") String end, ModelMap modelMap) {
        try
        {
            LocalDate startTime;
            LocalDate endTime;
            try {
                startTime = LocalDateTime.parse(start).toLocalDate();
                endTime = LocalDateTime.parse(end).toLocalDate();
            } catch (DateTimeParseException e1) {
                try {
                    startTime =  LocalDate.parse(start);
                    endTime =  LocalDate.parse(end);
                } catch (DateTimeParseException e2) {
                    throw new IllegalArgumentException("Invalid date format");
                }
            }
            List<CalenderEventDto> calenderEventDtoList = new ArrayList<>();
            List<ScheduledClass> scheduledClasses;
            List<ScheduledClass> repeatedSchedule;
            if(trainerId == null){
                scheduledClasses = scheduledClassService.getAllScheduledClasses(startTime,endTime, null);
                repeatedSchedule = scheduledClassService.getAllRepeatedSchedule(startTime,endTime, null);
            }
            else {
                scheduledClasses = scheduledClassService.getAllScheduledClasses(startTime,endTime, trainerId);
                repeatedSchedule = scheduledClassService.getAllRepeatedSchedule(startTime,endTime, trainerId);
                List<AppointmentInstance> appointmentInstances = appointmentInstanceService.getAllAppointmentService(startTime, endTime, trainerId);
                for(AppointmentInstance appointmentInstance : appointmentInstances){
                    Appointment item = appointmentInstance.getAppointment();
                    CalenderEventDto calenderEventDto = new CalenderEventDto();
                    calenderEventDto.setEventName("PT kèm riêng: " + item.getMember().getFirstName() + " " + item.getMember().getLastName());
                    calenderEventDto.setCategoryColor(item.getBackgroundColor());
                    calenderEventDto.setCategoryColorBorder(item.getBackgroundColor());
                    calenderEventDto.setEventId("appointment-" + appointmentInstance.getId().toString());
                    calenderEventDto.setStartTime(appointmentInstance.getDate().atTime(appointmentInstance.getFrom()));
                    calenderEventDto.setEndTime(appointmentInstance.getDate().atTime(appointmentInstance.getTo()));
                    calenderEventDto.setRecurring(appointmentInstance.getIsRepeat() == 1);
                    calenderEventDto.setType(0);
                    calenderEventDtoList.add(calenderEventDto);
                }

            }
            for(ScheduledClass scheduledClass : scheduledClasses){
                Integer numberOfAttendances = attendanceRepository.getNumberOfAttendance(scheduledClass.getId(), scheduledClass.getDate());
                CalenderEventDto item = new CalenderEventDto();
                item.setEventName(scheduledClass.getClasses().getName() + " (" + numberOfAttendances  + "/" + scheduledClass.getCapacity() + ")");
                item.setCategoryColor(scheduledClass.getBackgroundColor());
                item.setCategoryColorBorder(scheduledClass.getBackgroundColor());
                item.setEventId(scheduledClass.getId().toString());
                item.setStartTime(scheduledClass.getDate().atTime(scheduledClass.getFrom()));
                item.setEndTime(scheduledClass.getDate().atTime(scheduledClass.getTo()));
                item.setType(1);
                calenderEventDtoList.add(item);
            }
            for(ScheduledClass item : repeatedSchedule)
            {
                if(item.getRepeat() == RecurrenceType.WEEKLY)
                {
                    DayOfWeek dayOfWeek = item.getDate().getDayOfWeek();
                    List<LocalDate> dayOfWeekList = new ArrayList<>();
                    LocalDate firstCloseDOW = startTime.with(TemporalAdjusters.nextOrSame(dayOfWeek));
                    while (!firstCloseDOW.isAfter(endTime)) {
                        dayOfWeekList.add(firstCloseDOW);
                        firstCloseDOW = firstCloseDOW.plusWeeks(1);
                    }

                    dayOfWeekList.removeIf(date -> date.isBefore(item.getDate()));
                    if(item.getEndRecur()!=null)
                    {
                        dayOfWeekList.removeIf(date -> date.isAfter(item.getEndRecur()));
                    }
                    for(LocalDate dayOfWeekItem : dayOfWeekList )
                    {
                        Integer numberOfAttendances = attendanceRepository.getNumberOfAttendance(item.getId(), dayOfWeekItem);
                        CalenderEventDto calenderEventDto = new CalenderEventDto();
                        calenderEventDto.setEventName(item.getClasses().getName() + " (" + numberOfAttendances  + "/" + item.getCapacity() + ")");
                        calenderEventDto.setCategoryColor(item.getBackgroundColor());
                        calenderEventDto.setCategoryColorBorder(item.getBackgroundColor());
                        calenderEventDto.setEventId(item.getId().toString());
                        calenderEventDto.setStartTime(dayOfWeekItem.atTime(item.getFrom()));
                        calenderEventDto.setEndTime(dayOfWeekItem.atTime(item.getTo()));
                        calenderEventDto.setRecurring(true);
                        calenderEventDto.setType(1);
                        calenderEventDtoList.add(calenderEventDto);
                    }
                }
                else if(item.getRepeat() == RecurrenceType.FORTNIGHTLY)
                {
                    calenderEventDtoList.addAll(ProcessWeeklyWithInterval(2, item, startTime, endTime));

                }
                else if(item.getRepeat() == RecurrenceType.THREE_WEEKLY)
                {
                    calenderEventDtoList.addAll(ProcessWeeklyWithInterval(3, item, startTime, endTime));
                }
                else if(item.getRepeat() == RecurrenceType.FOUR_WEEKLY)
                {
                    calenderEventDtoList.addAll(ProcessWeeklyWithInterval(4, item, startTime, endTime));
                }
                else if(item.getRepeat() == RecurrenceType.SIX_WEEKLY)
                {
                    calenderEventDtoList.addAll(ProcessWeeklyWithInterval(6, item, startTime, endTime));
                }
                else if(item.getRepeat() == RecurrenceType.EIGHT_WEEKLY)
                {
                    calenderEventDtoList.addAll(ProcessWeeklyWithInterval(8, item, startTime, endTime));
                }
                else if(item.getRepeat() == RecurrenceType.MONTHLY)
                {
                    int date = item.getDate().getDayOfMonth();
                    LocalDate monthIterator = startTime.withDayOfMonth(1);
                    while(!monthIterator.isAfter(endTime))
                    {
                        YearMonth currentMonth = YearMonth.from(monthIterator);
                        if (currentMonth.isValidDay(date)) {
                            LocalDate occurrenceDate = currentMonth.atDay(date);
                            if (!occurrenceDate.isBefore(item.getDate()) &&
                                    (item.getEndRecur() == null || !occurrenceDate.isAfter(item.getEndRecur())) &&
                                    !occurrenceDate.isBefore(startTime) && !occurrenceDate.isAfter(endTime)) {
                                Integer numberOfAttendances = attendanceRepository.getNumberOfAttendance(item.getId(), occurrenceDate);
                                CalenderEventDto calenderEventDto = new CalenderEventDto();
                                calenderEventDto.setEventName(item.getClasses().getName() + " (" + numberOfAttendances  + "/" + item.getCapacity() + ")");
                                calenderEventDto.setCategoryColor(item.getBackgroundColor());
                                calenderEventDto.setCategoryColorBorder(item.getBackgroundColor());
                                calenderEventDto.setEventId(item.getId().toString());
                                calenderEventDto.setStartTime(occurrenceDate.atTime(item.getFrom()));
                                calenderEventDto.setEndTime(occurrenceDate.atTime(item.getTo()));
                                calenderEventDto.setRecurring(true);
                                calenderEventDto.setType(1);
                                calenderEventDtoList.add(calenderEventDto);
                            }

                        }
                        monthIterator = monthIterator.plusMonths(1);
                    }
                }
                else if(item.getRepeat() == RecurrenceType.DAILY)
                {
                    LocalDate currentDate = startTime;
                    while(!currentDate.isAfter(endTime))
                    {
                        if (!currentDate.isBefore(item.getDate()) &&
                                (item.getEndRecur() == null || !currentDate.isAfter(item.getEndRecur())) &&
                                !currentDate.isBefore(startTime) && !currentDate.isAfter(endTime)) {
                            Integer numberOfAttendances = attendanceRepository.getNumberOfAttendance(item.getId(), currentDate);
                            CalenderEventDto calenderEventDto = new CalenderEventDto();
                            calenderEventDto.setEventName(item.getClasses().getName() + " (" + numberOfAttendances  + "/" + item.getCapacity() + ")");
                            calenderEventDto.setCategoryColor(item.getBackgroundColor());
                            calenderEventDto.setCategoryColorBorder(item.getBackgroundColor());
                            calenderEventDto.setEventId(item.getId().toString());
                            calenderEventDto.setStartTime(currentDate.atTime(item.getFrom()));
                            calenderEventDto.setEndTime(currentDate.atTime(item.getTo()));
                            calenderEventDto.setRecurring(true);
                            calenderEventDto.setType(1);
                            calenderEventDtoList.add(calenderEventDto);
                        }
                        currentDate = currentDate.plusDays(1);
                    }
                }
            }
            List<BlockedTime> noRepeatBlockedTime = blockedTimeService.getAllNonRepeated(startTime, endTime);
            for (BlockedTime item : noRepeatBlockedTime) {
                CalenderEventDto calenderEventDto = new CalenderEventDto();
                calenderEventDto.setEventName("");
                calenderEventDto.setCategoryColor("rgba(170, 170, 170, 0.5)");
                calenderEventDto.setCategoryColorBorder("rgba(170, 170, 170, 0.5)");
                calenderEventDto.setEventId("blocked-" + item.getId());
                calenderEventDto.setStartTime(item.getDate().atTime(item.getFrom()));
                calenderEventDto.setEndTime(item.getDate().atTime(item.getTo()));
                calenderEventDto.setRecurring(false);
                calenderEventDto.setType(2);
                calenderEventDto.setOverlap(false);
                calenderEventDtoList.add(calenderEventDto);
            }
            List<BlockedTime> repeatBlockedTime = blockedTimeService.getAllRepeated(startTime, endTime);
            for(BlockedTime item : repeatBlockedTime) {
                if(item.getRepeat() == RecurrenceType.WEEKLY)
                {
                    DayOfWeek dayOfWeek = item.getDate().getDayOfWeek();
                    List<LocalDate> dayOfWeekList = new ArrayList<>();
                    LocalDate firstCloseDOW = startTime.with(TemporalAdjusters.nextOrSame(dayOfWeek));
                    while (!firstCloseDOW.isAfter(endTime)) {
                        dayOfWeekList.add(firstCloseDOW);
                        firstCloseDOW = firstCloseDOW.plusWeeks(1);
                    }

                    dayOfWeekList.removeIf(date -> date.isBefore(item.getDate()));
                    if(item.getEndRecur()!=null)
                    {
                        dayOfWeekList.removeIf(date -> date.isAfter(item.getEndRecur()));
                    }
                    for(LocalDate dayOfWeekItem : dayOfWeekList )
                    {
                        calenderEventDtoList.removeIf(calenderEventDto -> {
                            if(calenderEventDto.getType() < 2)
                            {
                                if(calenderEventDto.getStartTime().isBefore(dayOfWeekItem.atTime(item.getTo())) &&
                                        calenderEventDto.getEndTime().isAfter(dayOfWeekItem.atTime(item.getFrom())))
                                {
                                    return true;
                                }
                            }
                            return false;
                        });
                        CalenderEventDto calenderEventDto = new CalenderEventDto();
                        calenderEventDto.setEventName("");
                        calenderEventDto.setCategoryColor("rgba(170, 170, 170, 0.5)");
                        calenderEventDto.setCategoryColorBorder("rgba(170, 170, 170, 0.5)");
                        calenderEventDto.setEventId("blocked-" + item.getId());
                        calenderEventDto.setStartTime(dayOfWeekItem.atTime(item.getFrom()));
                        calenderEventDto.setEndTime(dayOfWeekItem.atTime(item.getTo()));
                        calenderEventDto.setRecurring(false);
                        calenderEventDto.setType(2);
                        calenderEventDto.setOverlap(false);
                        calenderEventDtoList.add(calenderEventDto);
                    }
                }
                else if(item.getRepeat() == RecurrenceType.FORTNIGHTLY)
                {
                    List<CalenderEventDto> tempList = ProcessWeeklyBlockedWithInterval(2, item, startTime, endTime);
                    calenderEventDtoList.removeIf(calenderEventDto -> {
                        if(calenderEventDto.getType() < 2)
                        {
                            for(CalenderEventDto dtoItem : tempList)
                            {
                                if(calenderEventDto.getStartTime().isBefore(dtoItem.getEndTime()) &&
                                        calenderEventDto.getEndTime().isAfter(dtoItem.getStartTime()))
                                {
                                    return true;
                                }
                            }
                        }
                        return false;
                    });
                    calenderEventDtoList.addAll(tempList);
                }
                else if(item.getRepeat() == RecurrenceType.THREE_WEEKLY)
                {
                    List<CalenderEventDto> tempList = ProcessWeeklyBlockedWithInterval(3, item, startTime, endTime);
                    calenderEventDtoList.removeIf(calenderEventDto -> {
                        if(calenderEventDto.getType() < 2)
                        {
                            for(CalenderEventDto dtoItem : tempList)
                            {
                                if(calenderEventDto.getStartTime().isBefore(dtoItem.getEndTime()) &&
                                        calenderEventDto.getEndTime().isAfter(dtoItem.getStartTime()))
                                {
                                    return true;
                                }
                            }
                        }
                        return false;
                    });
                    calenderEventDtoList.addAll(tempList);
                }
                else if(item.getRepeat() == RecurrenceType.FOUR_WEEKLY)
                {
                    List<CalenderEventDto> tempList = ProcessWeeklyBlockedWithInterval(4, item, startTime, endTime);
                    calenderEventDtoList.removeIf(calenderEventDto -> {
                        if(calenderEventDto.getType() < 2)
                        {
                            for(CalenderEventDto dtoItem : tempList)
                            {
                                if(calenderEventDto.getStartTime().isBefore(dtoItem.getEndTime()) &&
                                        calenderEventDto.getEndTime().isAfter(dtoItem.getStartTime()))
                                {
                                    return true;
                                }
                            }
                        }
                        return false;
                    });
                    calenderEventDtoList.addAll(tempList);
                }
                else if(item.getRepeat() == RecurrenceType.SIX_WEEKLY)
                {
                    List<CalenderEventDto> tempList = ProcessWeeklyBlockedWithInterval(6, item, startTime, endTime);
                    calenderEventDtoList.removeIf(calenderEventDto -> {
                        if(calenderEventDto.getType() < 2)
                        {
                            for(CalenderEventDto dtoItem : tempList)
                            {
                                if(calenderEventDto.getStartTime().isBefore(dtoItem.getEndTime()) &&
                                        calenderEventDto.getEndTime().isAfter(dtoItem.getStartTime()))
                                {
                                    return true;
                                }
                            }
                        }
                        return false;
                    });
                    calenderEventDtoList.addAll(tempList);
                }
                else if(item.getRepeat() == RecurrenceType.EIGHT_WEEKLY)
                {
                    List<CalenderEventDto> tempList = ProcessWeeklyBlockedWithInterval(8, item, startTime, endTime);
                    calenderEventDtoList.removeIf(calenderEventDto -> {
                        if(calenderEventDto.getType() < 2)
                        {
                            for(CalenderEventDto dtoItem : tempList)
                            {
                                if(calenderEventDto.getStartTime().isBefore(dtoItem.getEndTime()) &&
                                        calenderEventDto.getEndTime().isAfter(dtoItem.getStartTime()))
                                {
                                    return true;
                                }
                            }
                        }
                        return false;
                    });
                    calenderEventDtoList.addAll(tempList);
                }
                else if(item.getRepeat() == RecurrenceType.MONTHLY)
                {
                    int date = item.getDate().getDayOfMonth();
                    LocalDate monthIterator = startTime.withDayOfMonth(1);
                    while(!monthIterator.isAfter(endTime))
                    {
                        YearMonth currentMonth = YearMonth.from(monthIterator);
                        if (currentMonth.isValidDay(date)) {
                            LocalDate occurrenceDate = currentMonth.atDay(date);
                            if (!occurrenceDate.isBefore(item.getDate()) &&
                                    (item.getEndRecur() == null || !occurrenceDate.isAfter(item.getEndRecur())) &&
                                    !occurrenceDate.isBefore(startTime) && !occurrenceDate.isAfter(endTime)) {

                                calenderEventDtoList.removeIf(calenderEventDto -> {
                                    if(calenderEventDto.getType() < 2)
                                    {
                                        if(calenderEventDto.getStartTime().isBefore(occurrenceDate.atTime(item.getTo())) &&
                                                calenderEventDto.getEndTime().isAfter(occurrenceDate.atTime(item.getFrom())))
                                        {
                                            return true;
                                        }
                                    }
                                    return false;
                                });

                                CalenderEventDto calenderEventDto = new CalenderEventDto();
                                calenderEventDto.setEventName("");
                                calenderEventDto.setCategoryColor("rgba(170, 170, 170, 0.5)");
                                calenderEventDto.setCategoryColorBorder("rgba(170, 170, 170, 0.5)");
                                calenderEventDto.setEventId("blocked-" + item.getId());
                                calenderEventDto.setStartTime(occurrenceDate.atTime(item.getFrom()));
                                calenderEventDto.setEndTime(occurrenceDate.atTime(item.getTo()));
                                calenderEventDto.setRecurring(false);
                                calenderEventDto.setType(2);
                                calenderEventDto.setOverlap(false);
                                calenderEventDtoList.add(calenderEventDto);
                            }

                        }
                        monthIterator = monthIterator.plusMonths(1);
                    }
                }
                else if(item.getRepeat() == RecurrenceType.DAILY)
                {
                    LocalDate currentDate = startTime;
                    while(!currentDate.isAfter(endTime))
                    {
                        if (!currentDate.isBefore(item.getDate()) &&
                                (item.getEndRecur() == null || !currentDate.isAfter(item.getEndRecur())) &&
                                !currentDate.isBefore(startTime) && !currentDate.isAfter(endTime)) {

                            LocalDate finalCurrentDate = currentDate;
                            calenderEventDtoList.removeIf(calenderEventDto -> {
                                if(calenderEventDto.getType() < 2)
                                {
                                    if(calenderEventDto.getStartTime().isBefore(finalCurrentDate.atTime(item.getTo())) &&
                                            calenderEventDto.getEndTime().isAfter(finalCurrentDate.atTime(item.getFrom())))
                                    {
                                        return true;
                                    }
                                }
                                return false;
                            });

                            CalenderEventDto calenderEventDto = new CalenderEventDto();
                            calenderEventDto.setEventName("");
                            calenderEventDto.setCategoryColor("rgba(170, 170, 170, 0.5)");
                            calenderEventDto.setCategoryColorBorder("rgba(170, 170, 170, 0.5)");
                            calenderEventDto.setEventId("blocked-" + item.getId());
                            calenderEventDto.setStartTime(currentDate.atTime(item.getFrom()));
                            calenderEventDto.setEndTime(currentDate.atTime(item.getTo()));
                            calenderEventDto.setRecurring(false);
                            calenderEventDto.setType(2);
                            calenderEventDto.setOverlap(false);
                            calenderEventDtoList.add(calenderEventDto);
                        }
                        currentDate = currentDate.plusDays(1);
                    }
                }
            }
            calenderEventDtoList.removeIf(item -> {
                if(item.getType() < 2)
                {
                    for(BlockedTime noRepeat : noRepeatBlockedTime)
                    {
                        if(item.getStartTime().isBefore(noRepeat.getDate().atTime(noRepeat.getTo())) &&
                                item.getEndTime().isAfter(noRepeat.getDate().atTime(noRepeat.getFrom())))
                        {
                            return true;
                        }
                    }

                }
                return false;
            });
            return calenderEventDtoList;
        }
        catch (Exception e)
        {
            log.error(e.getMessage());
            return new ArrayList<>();
        }
    }

    private List<CalenderEventDto> ProcessWeeklyWithInterval(Integer interval, ScheduledClass item, LocalDate startDate, LocalDate endDate)
    {
        List<CalenderEventDto> calenderEventDtoList = new ArrayList<>();
        DayOfWeek dayOfWeek = item.getDate().getDayOfWeek();
        List<LocalDate> dayOfWeekList = new ArrayList<>();
        LocalDate firstCloseDOW = startDate.with(TemporalAdjusters.nextOrSame(dayOfWeek));
        while (!firstCloseDOW.isAfter(endDate)) {
            dayOfWeekList.add(firstCloseDOW);
            firstCloseDOW = firstCloseDOW.plusWeeks(1);
        }
        dayOfWeekList = dayOfWeekList.stream()
                .filter(candidate -> {
                    long weeksBetween = ChronoUnit.WEEKS.between(item.getDate(), candidate);
                    return weeksBetween >= 0 && weeksBetween % interval == 0;
                })
                .collect(Collectors.toList());
        dayOfWeekList.removeIf(date -> date.isBefore(item.getDate()));
        if(item.getEndRecur()!=null)
        {
            dayOfWeekList.removeIf(date -> date.isAfter(item.getEndRecur()));
        }
        for(LocalDate dayOfWeekItem : dayOfWeekList )
        {
            Integer numberOfAttendances = attendanceRepository.getNumberOfAttendance(item.getId(), dayOfWeekItem);
            CalenderEventDto calenderEventDto = new CalenderEventDto();
            calenderEventDto.setEventName(item.getClasses().getName() + " (" + numberOfAttendances  + "/" + item.getCapacity() + ")");
            calenderEventDto.setCategoryColor(item.getBackgroundColor());
            calenderEventDto.setCategoryColorBorder(item.getBackgroundColor());
            calenderEventDto.setEventId(item.getId().toString());
            calenderEventDto.setStartTime(dayOfWeekItem.atTime(item.getFrom()));
            calenderEventDto.setEndTime(dayOfWeekItem.atTime(item.getTo()));
            calenderEventDto.setRecurring(true);
            calenderEventDto.setType(1);
            calenderEventDtoList.add(calenderEventDto);
        }
        return calenderEventDtoList;
    }

    private List<CalenderEventDto> ProcessWeeklyAppointmentWithInterval(Integer interval, Appointment item, LocalDate startDate, LocalDate endDate)
    {
        List<CalenderEventDto> calenderEventDtoList = new ArrayList<>();
        DayOfWeek dayOfWeek = item.getDate().getDayOfWeek();
        List<LocalDate> dayOfWeekList = new ArrayList<>();
        LocalDate firstCloseDOW = startDate.with(TemporalAdjusters.nextOrSame(dayOfWeek));
        while (!firstCloseDOW.isAfter(endDate)) {
            dayOfWeekList.add(firstCloseDOW);
            firstCloseDOW = firstCloseDOW.plusWeeks(1);
        }
        dayOfWeekList = dayOfWeekList.stream()
                .filter(candidate -> {
                    long weeksBetween = ChronoUnit.WEEKS.between(item.getDate(), candidate);
                    return weeksBetween >= 0 && weeksBetween % interval == 0;
                })
                .collect(Collectors.toList());
        dayOfWeekList.removeIf(date -> date.isBefore(item.getDate()));
        if(item.getEndRecur()!=null)
        {
            dayOfWeekList.removeIf(date -> date.isAfter(item.getEndRecur()));
        }
        for(LocalDate dayOfWeekItem : dayOfWeekList )
        {
            CalenderEventDto calenderEventDto = new CalenderEventDto();
            calenderEventDto.setEventName("PT kèm riêng: " + item.getMember().getFirstName() + " " + item.getMember().getLastName());
            calenderEventDto.setCategoryColor(item.getBackgroundColor());
            calenderEventDto.setCategoryColorBorder(item.getBackgroundColor());
            calenderEventDto.setEventId("appointment-" + item.getId().toString());
            calenderEventDto.setStartTime(dayOfWeekItem.atTime(item.getFrom()));
            calenderEventDto.setEndTime(dayOfWeekItem.atTime(item.getTo()));
            calenderEventDto.setRecurring(true);
            calenderEventDto.setType(0);
            calenderEventDtoList.add(calenderEventDto);
        }
        return calenderEventDtoList;
    }

    private List<CalenderEventDto> ProcessWeeklyBlockedWithInterval(Integer interval, BlockedTime item, LocalDate startDate, LocalDate endDate)
    {
        List<CalenderEventDto> calenderEventDtoList = new ArrayList<>();
        DayOfWeek dayOfWeek = item.getDate().getDayOfWeek();
        List<LocalDate> dayOfWeekList = new ArrayList<>();
        LocalDate firstCloseDOW = startDate.with(TemporalAdjusters.nextOrSame(dayOfWeek));
        while (!firstCloseDOW.isAfter(endDate)) {
            dayOfWeekList.add(firstCloseDOW);
            firstCloseDOW = firstCloseDOW.plusWeeks(1);
        }
        dayOfWeekList = dayOfWeekList.stream()
                .filter(candidate -> {
                    long weeksBetween = ChronoUnit.WEEKS.between(item.getDate(), candidate);
                    return weeksBetween >= 0 && weeksBetween % interval == 0;
                })
                .collect(Collectors.toList());
        dayOfWeekList.removeIf(date -> date.isBefore(item.getDate()));
        if(item.getEndRecur()!=null)
        {
            dayOfWeekList.removeIf(date -> date.isAfter(item.getEndRecur()));
        }
        for(LocalDate dayOfWeekItem : dayOfWeekList )
        {
            CalenderEventDto calenderEventDto = new CalenderEventDto();
            calenderEventDto.setEventName("");
            calenderEventDto.setCategoryColor("rgba(170, 170, 170, 0.5)");
            calenderEventDto.setCategoryColorBorder("rgba(170, 170, 170, 0.5)");
            calenderEventDto.setEventId("blocked-" + item.getId());
            calenderEventDto.setStartTime(dayOfWeekItem.atTime(item.getFrom()));
            calenderEventDto.setEndTime(dayOfWeekItem.atTime(item.getTo()));
            calenderEventDto.setRecurring(false);
            calenderEventDto.setType(2);
            calenderEventDto.setOverlap(false);
            calenderEventDtoList.add(calenderEventDto);
        }
        return calenderEventDtoList;
    }

    @PutMapping("/update/{id}/{type}/{deleteType}")
    @ResponseBody
    public ResponseEntity<String> updateEvent(
            @PathVariable Long id,
            @PathVariable Integer type,
            @PathVariable String deleteType,
            @RequestBody CalenderEventDto eventDto) {

        try {
            if(type == 1)
            {
                scheduledClassService.updateScheduledClass(id, eventDto);
            }
//            else if(type == 2)
//            {
//                blockedTimeService.updateBlockedTime(id, eventDto);
//            }
            else if(type == 0)
            {
                appointmentInstanceService.updateAppointmentInstance(id, eventDto, deleteType);
            }
            return ResponseEntity.ok().build();
        }
        catch (ClassInSessionException e)
        {
            log.error("Error updating event: " + e.getMessage());
            return ResponseEntity.status(500).body(e.getMessage());
        }
        catch (Exception e) {
            log.error("Error updating event: " + e.getMessage());
            return ResponseEntity.status(500).body("Xảy ra lỗi khi cập nhật lịch");
        }
    }

    @GetMapping("/create-option")
    public String create(@RequestParam(value = "start") String start,
                         @RequestParam(value = "end") String end,
                         @RequestParam(value = "trainerId", required = false) String trainerId, ModelMap model) {
        try {
            LocalDate startDate;
            LocalDate endDate;
            LocalTime startTime;
            LocalTime endTime;
            try {
                startDate = LocalDateTime.parse(start).toLocalDate();
                endDate = LocalDateTime.parse(end).toLocalDate();
                startTime = LocalDateTime.parse(start).toLocalTime();
                endTime = LocalDateTime.parse(end).toLocalTime();
            } catch (DateTimeParseException e1) {
                try {
                    startDate =  LocalDate.parse(start);
                    endDate =  LocalDate.parse(end);
                    startTime = endTime = null;
                } catch (DateTimeParseException e2) {
                    throw new IllegalArgumentException("Invalid date format");
                }
            }
            Long trainerIdLong;
            if(!trainerId.equals("null"))
            {
                trainerIdLong = Long.parseLong(trainerId);
            }
            else {
                trainerIdLong = null;
            }
            LocalDateTime threshold = LocalDateTime.now().plusHours(12);
            if(threshold.isAfter(startDate.atTime(startTime))) {
                model.put("allowCreateAppointment", false);
            }
            else {
                model.put("allowCreateAppointment", true);
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String formattedStartDate = startDate.format(formatter);
            String formattedEndDate = endDate.format(formatter);
            model.put("startDate", formattedStartDate);
            model.put("endDate", formattedEndDate);
            model.put("startTime", startTime);
            model.put("endTime", endTime);
            model.put("trainerId", trainerIdLong);
            return "gym/fragment/schedule :: createScheduleOption";
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            return "404";
        }
    }

    @GetMapping("/update-option")
    public String update(@RequestParam(value = "id") Long id,
                         @RequestParam(value = "type") Integer type,
                         @RequestParam(value = "date", required = false) String date,
                         @RequestParam(value = "trainerId", required = false) String trainerId, ModelMap model) {
        try {
            if(type == 1)
            {
                ScheduledClass scheduledClass = scheduledClassRepository.findById(id).orElseThrow();
                model.put("model", scheduledClass);
                LocalDateTime dateTime = LocalDateTime.parse(date);
                Integer numberOfAttendances = attendanceRepository.getNumberOfAttendance(scheduledClass.getId(), dateTime.toLocalDate());
                model.put("numberOfAttendances", numberOfAttendances);
            }
            else if(type == 0)
            {
                AppointmentInstance appointmentInstance = appointmentInstanceRepository.findById(id).orElseThrow();
                Appointment appointment = appointmentInstance.getAppointment();
                appointment.setDate(appointmentInstance.getDate());
                appointment.setFrom(appointmentInstance.getFrom());
                appointment.setTo(appointmentInstance.getTo());
                appointment.setStatus(appointmentInstance.getStatus());
                model.put("model", appointment);
                model.put("appointmentInstanceId", appointmentInstance.getId());
                LocalDateTime threshold = LocalDateTime.now().plusHours(12);
                if(threshold.isAfter(appointmentInstance.getDate().atTime(appointmentInstance.getFrom())))
                {
                   model.put("allowCancelAndUpdate", false);
                }
                else model.put("allowCancelAndUpdate", true);
            }
            else {
                BlockedTime blockedTime = blockedTimeRepository.findById(id).orElseThrow();
                model.put("model", blockedTime);
            }
            Long trainerIdLong;
            if(!trainerId.equals("null"))
            {
                trainerIdLong = Long.parseLong(trainerId);
            }
            else {
                trainerIdLong = null;
            }
            model.put("type", type);
            model.put("date", date);
            model.put("trainerId", trainerIdLong);
            return "gym/fragment/schedule :: updateScheduleOption";
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            return "404";
        }
    }

    @RequestMapping("/form")
    public String createSchedule(@RequestParam(name = "view") String view,
                                 @RequestParam(name = "date") String date,
                                 @RequestParam(name = "trainerId", required = false) Long trainerId,
                                 @RequestParam(name = "startDate", required = false) String startDateStr,
                                 @RequestParam(name = "startTime", required = false) String startTime,
                                 @RequestParam(name = "endTime", required = false) String endTime,
                                 @RequestParam(name = "id", required = false) Long id, ModelMap model) {
        model.put("title", id != null ? "Sửa lớp" : "Thêm lớp");
        ScheduleDto formDto = (ScheduleDto) model.getOrDefault("form", new ScheduleDto());
        if (id != null) {
            ScheduledClass scheduledClass = scheduledClassRepository.findById(id).orElseThrow();
            if (!model.containsAttribute("form")) {
                formDto = modelMapper.map(scheduledClass, ScheduleDto.class);
                formDto.setClassId(scheduledClass.getClasses().getId());
                formDto.setTrainerId(scheduledClass.getTrainer().getId());
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                formDto.setStartDateStr(scheduledClass.getDate().format(dateFormatter));
                formDto.setFromStr(scheduledClass.getFrom().format(timeFormatter));
                formDto.setToStr(scheduledClass.getTo().format(timeFormatter));
                formDto.setView(view);
                formDto.setDate(date);
                if(scheduledClass.getEndRecur()!=null)
                {
                    formDto.setEndRecurStr(scheduledClass.getEndRecur().format(dateFormatter));
                }
            }
        }
        else {
            if (!model.containsAttribute("form"))
            {
                formDto.setStartDateStr(startDateStr);
                formDto.setView(view);
                formDto.setDate(date);
                formDto.setFromStr(startTime);
                formDto.setToStr(endTime);
            }
        }

        model.putIfAbsent("form", formDto);

        if (formDto.getClassId() != null) {
            classRepository.findById(Long.parseLong(formDto.getClassId().toString())).ifPresent(item -> {
                model.put("className", item.getName());
            });
        }
        if (formDto.getTrainerId() != null) {
            trainerRepository.findById(Long.parseLong(formDto.getTrainerId().toString())).ifPresent(item -> {
                model.put("trainerName", item.getFirstName() + " " + item.getLastName());
            });
        }
        model.put("trainerId", trainerId);
        return "gym/schedule/createClass";
    }

    @RequestMapping("/form-appointment")
    public String formAppointment(@RequestParam(name = "view") String view,
                                 @RequestParam(name = "date") String date,
                                 @RequestParam(name = "trainerId", required = false) Long trainerId,
                                 @RequestParam(name = "startDate", required = false) String startDateStr,
                                 @RequestParam(name = "startTime", required = false) String startTime,
                                 @RequestParam(name = "endTime", required = false) String endTime,
                                 @RequestParam(name = "id", required = false) Long id,
                                 @RequestParam(name = "appointmentInstanceId", required = false) Long appointmentInstanceId,ModelMap model) {
        model.put("title", id != null ? "Sửa buổi" : "Thêm buổi");
        AppointmentDto formDto = (AppointmentDto) model.getOrDefault("form", new AppointmentDto());
        if (id != null) {
            Appointment appointment = appointmentRepository.findById(id).orElseThrow();
            AppointmentInstance appointmentInstance = appointmentInstanceRepository.findById(appointmentInstanceId).orElseThrow();
            if (!model.containsAttribute("form")) {
                formDto = modelMapper.map(appointment, AppointmentDto.class);
                formDto.setMemberId(appointment.getMember().getId());
                formDto.setTrainerId(appointment.getTrainer().getId());
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                formDto.setStartDateStr(appointmentInstance.getDate().format(dateFormatter));
                formDto.setFromStr(appointmentInstance.getFrom().format(timeFormatter));
                formDto.setToStr(appointmentInstance.getTo().format(timeFormatter));
                formDto.setView(view);
                formDto.setDate(date);
                formDto.setAppointmentInstanceId(appointmentInstanceId);
                if(appointment.getEndRecur()!=null)
                {
                    formDto.setEndRecurStr(appointment.getEndRecur().format(dateFormatter));
                }
            }
            model.put("isRecurring", !appointment.getRepeat().equals(RecurrenceType.NONE));
            if(!appointment.getRepeat().equals(RecurrenceType.NONE))
            {
                model.put("initialDateStr", appointmentInstance.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                model.put("initialFromStr", appointmentInstance.getFrom().format(DateTimeFormatter.ofPattern("HH:mm")));
                model.put("initialToStr", appointmentInstance.getTo().format(DateTimeFormatter.ofPattern("HH:mm")));
            }
            model.put("initialRecurringType", appointment.getRepeat().toString());
        }
        else {
            if (!model.containsAttribute("form"))
            {
                formDto.setStartDateStr(startDateStr);
                formDto.setView(view);
                formDto.setDate(date);
                formDto.setFromStr(startTime);
                formDto.setToStr(endTime);
                formDto.setTrainerId(trainerId);
            }
        }

        model.putIfAbsent("form", formDto);

        if (formDto.getMemberId() != null) {
            memberRepository.findById(Long.parseLong(formDto.getMemberId().toString())).ifPresent(item -> {
                model.put("memberName", item.getFirstName() + " " + item.getLastName());
            });
        }
        model.put("trainerId", trainerId);
        return "gym/appointment/form";
    }

    @PostMapping("/save-class")
    public String save(@RequestParam(name = "returnTrainerId", required = false) Long returnTrainerId,
                       @Valid @ModelAttribute("form") ScheduleDto formDto,
                       BindingResult bindingResult,
                       RedirectAttributes redirectAttributes,
                       HttpServletRequest request) {
        try {
            if (bindingResult.hasErrors()) {
                return AppUtils.goBackWithError(request, redirectAttributes, "form", bindingResult, formDto, null)
                        .orElse("redirect:/schedule/form?view=" + formDto.getView() + "&date="
                                + formDto.getDate() + "&startDate=" + formDto.getStartDateStr() + "&startTime="
                                + formDto.getFromStr() + "&endTime=" + formDto.getToStr());
            }
            scheduledClassService.saveClass(formDto);
            String trainerIdString = "";
            if(returnTrainerId!=null){
                trainerIdString =  "&trainerId=" + returnTrainerId;
            }
            if (formDto.getId() == null) {
                redirectAttributes.addFlashAttribute("success", "Tạo thành công!");
                return "redirect:/schedule/index?view=" + formDto.getView() + "&date=" + formDto.getDate() + trainerIdString ;
            } else {
                redirectAttributes.addFlashAttribute("success", "Cập nhật thành công!");
            }
            return "redirect:/schedule/index?view=" + formDto.getView() + "&date=" + formDto.getDate() + trainerIdString;
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            return AppUtils.goBackWithError(request, redirectAttributes, "form", bindingResult, formDto, "Error in server!")
                    .orElse("redirect:/schedule/form?view=" + formDto.getView() + "&date="
                            + formDto.getDate() + "&startDate=" + formDto.getStartDateStr() + "&startTime="
                            + formDto.getFromStr() + "&endTime=" + formDto.getToStr());
        }
    }

    @PostMapping("/save-booking")
    public String save(@RequestParam(name = "returnTrainerId", required = false) Long returnTrainerId,
                       @RequestParam(name = "updateType", required = false) String updateType,
                       @Valid @ModelAttribute("form") AppointmentDto formDto,
                       BindingResult bindingResult,
                       RedirectAttributes redirectAttributes,
                       HttpServletRequest request) {
        try {
            if (bindingResult.hasErrors()) {
                return AppUtils.goBackWithError(request, redirectAttributes, "form", bindingResult, formDto, null)
                        .orElse("redirect:/schedule/form?view=" + formDto.getView() + "&date="
                                + formDto.getDate() + "&startDate=" + formDto.getStartDateStr() + "&startTime="
                                + formDto.getFromStr() + "&endTime=" + formDto.getToStr());
            }
            appointmentService.save(formDto, updateType);
            String trainerIdString = "";
            if(returnTrainerId!=null){
                trainerIdString =  "&trainerId=" + returnTrainerId;
            }
            if (formDto.getId() == null) {
                redirectAttributes.addFlashAttribute("success", "Tạo thành công!");
                return "redirect:/schedule/index?view=" + formDto.getView() + "&date=" + formDto.getDate() + trainerIdString ;
            } else {
                redirectAttributes.addFlashAttribute("success", "Cập nhật thành công!");
            }
            return "redirect:/schedule/index?view=" + formDto.getView() + "&date=" + formDto.getDate() + trainerIdString;
        }
        catch (CustomExceptionWithText e) {
            log.error("Exception: {}", e.getMessage(), e);
            return AppUtils.goBackWithError(request, redirectAttributes, "form", bindingResult, formDto, e.getMessage())
                    .orElse("redirect:/schedule/form?view=" + formDto.getView() + "&date="
                            + formDto.getDate() + "&startDate=" + formDto.getStartDateStr() + "&startTime="
                            + formDto.getFromStr() + "&endTime=" + formDto.getToStr());
        }
        catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            return AppUtils.goBackWithError(request, redirectAttributes, "form", bindingResult, formDto, "Error in server!")
                    .orElse("redirect:/schedule/form?view=" + formDto.getView() + "&date="
                            + formDto.getDate() + "&startDate=" + formDto.getStartDateStr() + "&startTime="
                            + formDto.getFromStr() + "&endTime=" + formDto.getToStr());
        }
    }

    @PostMapping(value = {"/delete-class/{id}/{type}"})
    public String delete(@PathVariable(required = true) Long id,
                         @PathVariable Integer type,
                         HttpServletRequest request,
                         RedirectAttributes redirectAttributes) {
        log.info("id: {}", id);
        try {
            if(type == 1)
            {
                LocalDateTime now = LocalDateTime.now();
                LocalDate currentDate = now.toLocalDate();
                LocalTime currentTime = now.toLocalTime();
                ScheduledClass scheduledClass = scheduledClassRepository.findById(id).orElseThrow();
                List<Attendance> attendanceList = attendanceRepository.findByScheduleIdAndDate(id, null);
                for(Attendance attendance : attendanceList)
                {
                    if(attendance.getBookingTime().equals(currentDate)) {
                        if(scheduledClass.getFrom().isAfter(currentTime) || scheduledClass.getTo().isAfter(currentTime))
                        {
                            Member member = memberRepository.findById(attendance.getMember().getId()).orElseThrow();
                            member.setCoin(member.getCoin()+scheduledClass.getPrice());
                            memberRepository.save(member);
                        }
                    }
                    else if(attendance.getBookingTime().isAfter(currentDate)) {
                        Member member = memberRepository.findById(attendance.getMember().getId()).orElseThrow();
                        member.setCoin(member.getCoin()+scheduledClass.getPrice());
                        memberRepository.save(member);
                    }
                    attendance.setStatus(0);
                    attendanceRepository.save(attendance);
                }
                scheduledClass.setStatus(0);
                scheduledClassRepository.save(scheduledClass);
            }
            else if(type == 2)
            {
                BlockedTime blockedTime = blockedTimeRepository.findById(id).orElseThrow();
                blockedTimeRepository.delete(blockedTime);
            }
            else if(type == 0){


            }
            redirectAttributes.addFlashAttribute("success", "Xóa thành công!");
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error in server!");
        }
        return AppUtils.goBack(request).orElse("redirect:/schedule/index");
    }

    @PostMapping(value = {"delete-appointment/{id}/{type}"})
    public String deleteAppointment(@PathVariable(required = true) Long id,
                         @PathVariable String type,
                         HttpServletRequest request,
                         RedirectAttributes redirectAttributes) {
        log.info("id: {}", id);
        try {
            appointmentService.delete(id, type);
            redirectAttributes.addFlashAttribute("success", "Xóa thành công!");
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error in server!");
        }
        return AppUtils.goBack(request).orElse("redirect:/schedule/index");
    }

//    @RequestMapping("/form-blocked-time")
//    public String createSchedule(@RequestParam(name = "view") String view,
//                                 @RequestParam(name = "date") String date,
//                                 @RequestParam(name = "startDate", required = false) String startDateStr,
//                                 @RequestParam(name = "startTime", required = false) String startTime,
//                                 @RequestParam(name = "endTime", required = false) String endTime,
//                                 @RequestParam(name = "id", required = false) Long id,
//                                 @RequestParam(name = "trainerId", required = false) Long trainerId, ModelMap model) {
//        model.put("title", id != null ? "Sửa thời gian chặn" : "Thêm thời gian chặn");
//        BlockedTimeDto formDto = (BlockedTimeDto) model.getOrDefault("form", new BlockedTimeDto());
//        if (id != null) {
//            BlockedTime blockedTime = blockedTimeRepository.findById(id).orElseThrow();
//            if (!model.containsAttribute("form")) {
//                formDto = modelMapper.map(blockedTime, BlockedTimeDto.class);
//                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
//                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
//                formDto.setStartDateStr(blockedTime.getDate().format(dateFormatter));
//                formDto.setFromStr(blockedTime.getFrom().format(timeFormatter));
//                formDto.setToStr(blockedTime.getTo().format(timeFormatter));
//                formDto.setDate(date);
//                formDto.setView(view);
//                if(blockedTime.getTrainer()!=null)
//                {
//                    formDto.setTrainerId(blockedTime.getTrainer().getId());
//                }
//                if(blockedTime.getEndRecur()!=null)
//                {
//                    formDto.setEndRecurStr(blockedTime.getEndRecur().format(dateFormatter));
//                }
//            }
//        }
//        else {
//            if (!model.containsAttribute("form"))
//            {
//                formDto.setStartDateStr(startDateStr);
//                formDto.setView(view);
//                formDto.setDate(date);
//                formDto.setFromStr(startTime);
//                formDto.setToStr(endTime);
//                formDto.setTrainerId(trainerId);
//            }
//        }
//
//        model.putIfAbsent("form", formDto);
//
//        if (formDto.getTrainerId() != null) {
//            trainerRepository.findById(Long.parseLong(formDto.getTrainerId().toString())).ifPresent(item -> {
//                model.put("trainerName", item.getFirstName() + " " + item.getLastName());
//            });
//        }
//        return "gym/blockedTime/form";
//    }

    @GetMapping("/block-event")
    public String save(@RequestParam(name = "id") Long id,
                       @RequestParam(name = "view") String view,
                       @RequestParam(name = "date") String date,
                       @RequestParam(name = "dateStr") String dateStr,
                       @RequestParam(name = "trainerId", required = false) Long trainerId,
                       RedirectAttributes redirectAttributes,
                       HttpServletRequest request) {
        try {
            ScheduledClass scheduledClass = scheduledClassRepository.findById(id).orElseThrow();
            LocalDateTime now = LocalDateTime.now();
            LocalDate currentDate = now.toLocalDate();
            LocalTime currentTime = now.toLocalTime();
            LocalDate blockedDate = LocalDateTime.parse(dateStr).toLocalDate();
            if(blockedDate.equals(currentDate)) {
                if(scheduledClass.getFrom().isAfter(currentTime) || scheduledClass.getTo().isAfter(currentTime))
                {
                    List<Attendance> attendanceList = attendanceRepository.findByScheduleIdAndDate(id, blockedDate);
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
            else if(blockedDate.isAfter(currentDate)) {
                List<Attendance> attendanceList = attendanceRepository.findByScheduleIdAndDate(id, blockedDate);
                for (Attendance attendance : attendanceList)
                {
                    attendance.setStatus(0);
                    attendanceRepository.save(attendance);
                    Member member = memberRepository.findById(attendance.getMember().getId()).orElseThrow();
                    member.setCoin(member.getCoin()+scheduledClass.getPrice());
                    memberRepository.save(member);
                }
            }
            BlockedTimeDto formDto = new BlockedTimeDto();
            formDto.setStartDateStr(blockedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            formDto.setFromStr(scheduledClass.getFrom().format(DateTimeFormatter.ofPattern("HH:mm")));
            formDto.setToStr(scheduledClass.getTo().format(DateTimeFormatter.ofPattern("HH:mm")));
            formDto.setRepeat(RecurrenceType.NONE.toString());
            blockedTimeService.save(formDto);
            redirectAttributes.addFlashAttribute("success", "Cập nhật thành công!");
            String trainerIdString = "";
            if(trainerId!=null){
                trainerIdString =  "&trainerId=" + trainerId;
            }
            return "redirect:/schedule/index?view=" + view + "&date=" + date + trainerIdString;
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Cập nhật thất bại!");
            return AppUtils.goBack(request).orElse("redirect:/schedule/index?view=" + view + "&date=" + date);
        }
    }

    @RequestMapping(value = {"/attendance/index"})
    public String index(@RequestParam(name = "id") Long id,
                        @RequestParam(name = "dateStr") String dateStr,
                        @RequestParam(name = "view") String view,
                        @RequestParam(name = "date") String date,
                        @RequestParam(name = "trainerId", required = false) Long trainerId,
                        @RequestParam(name = "pageNo", required = false, defaultValue = "1") Integer pageNo,
                        @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                        @RequestParam(name = "name", required = false) String name,
                        @RequestParam(name = "gender", required = false) Integer gender,
                        @RequestParam(name = "email", required = false) String email,
                        @RequestParam(name = "phoneNumber", required = false) String phoneNumber,
                        ModelMap model) {
        try
        {
            if(pageNo == null || pageNo <= 0) pageNo = 1;
            if(pageSize == null || pageSize <= 0) pageSize = 10;
            LocalDateTime now = LocalDateTime.now();
            LocalDate currentDate = now.toLocalDate();
            LocalTime currentTime = now.toLocalTime();
            Page<Member> pageObject = attendanceService.getPage(id,dateStr, name, gender, email, phoneNumber, pageNo, pageSize);
            List<Member> object = pageObject.getContent();
            for(Member member : object){
                member.setStatus(attendanceService.checkMemberStatus(id, member.getId(), dateStr));
            }
            ScheduledClass scheduledClass = scheduledClassRepository.findById(id).orElseThrow();
            LocalDateTime dateTime = LocalDateTime.parse(dateStr);
            String formatted = dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            model.put("title", scheduledClass.getClasses().getName()
                    + " vào " + formatted + " "
                    + scheduledClass.getFrom().format(DateTimeFormatter.ofPattern("HH/mm")) + " - "
                    + scheduledClass.getTo().format(DateTimeFormatter.ofPattern("HH:mm")));
            boolean allowAdd = false;
            if(now.isBefore(dateTime.toLocalDate().atTime(scheduledClass.getFrom())))
            {
                allowAdd = true;
            }
            model.put("pageNo", pageNo);
            model.put("pageSize", pageSize);
            model.put("totalPage", pageObject.getTotalPages());
            model.put("models", object);
            model.put("name", name);
            model.put("gender", gender);
            model.put("email", email);
            model.put("phoneNumber", phoneNumber);
            model.put("view", view);
            model.put("date", date);
            model.put("id", id);
            model.put("dateStr", dateStr);
            model.put("trainerId", trainerId);
            model.put("allowAdd", allowAdd);
            model.put("signUpAmount", scheduledClass.getPrice());
            return "gym/attendance/index";
        }
        catch (Exception e)
        {
            log.error("Exception: {}", e.getMessage(), e);
            return "redirect:/schedule/index";
        }
    }

}
