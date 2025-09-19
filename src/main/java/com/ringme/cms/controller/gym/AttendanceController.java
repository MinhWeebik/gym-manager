package com.ringme.cms.controller.gym;

import com.ringme.cms.model.gym.Attendance;
import com.ringme.cms.model.gym.Member;
import com.ringme.cms.model.gym.Membership;
import com.ringme.cms.model.gym.ScheduledClass;
import com.ringme.cms.repository.gym.AttendanceRepository;
import com.ringme.cms.repository.gym.ScheduledClassRepository;
import com.ringme.cms.service.gym.AttendanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

@Controller
@Log4j2
@RequestMapping("/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    private final ScheduledClassRepository  scheduledClassRepository;

    private final AttendanceRepository attendanceRepository;

    @GetMapping("/get-content-ids")
    public ResponseEntity<?> getContentIds(
            @RequestParam("id") Long id,
            @RequestParam("date") String date) {
        try {
            LocalDateTime ldt = LocalDateTime.parse(date);
            Set<Long> contentIds = attendanceRepository.findMemberIdsFromAttendanceId(id, ldt.toLocalDate());
            return ResponseEntity.ok(contentIds);
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error");
        }
    }

    @PostMapping("/add")
    public ResponseEntity<?> addContent(@RequestParam(name = "scheduledClassId") Long scheduledClassId,
                                        @RequestParam("memberId") Long memberId,
                                        @RequestParam("bookingTime") String bookingTime) {
        try {
            attendanceService.add(scheduledClassId, memberId, bookingTime);
            return ResponseEntity.ok("success");
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("error");
        }
    }

    @PostMapping("/remove")
    public ResponseEntity<?> removeContent(@RequestParam(name = "scheduledClassId") Long scheduledClassId,
                                        @RequestParam("memberId") Long memberId,
                                        @RequestParam("bookingTime") String bookingTime) {
        try {
            attendanceService.remove(scheduledClassId, memberId, bookingTime);
            return ResponseEntity.ok("success");
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("error");
        }
    }
}
