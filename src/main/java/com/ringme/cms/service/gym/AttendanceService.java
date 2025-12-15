package com.ringme.cms.service.gym;

import com.ringme.cms.model.gym.*;
import com.ringme.cms.repository.gym.AttendanceRepository;
import com.ringme.cms.repository.gym.MemberRepository;
import com.ringme.cms.repository.gym.ScheduledClassInstanceRepository;
import com.ringme.cms.repository.gym.ScheduledClassRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Log4j2
public class AttendanceService {

    private final MemberRepository memberRepository;

    private final ScheduledClassRepository scheduledClassRepository;

    private final AttendanceRepository attendanceRepository;
    private final ScheduledClassInstanceRepository scheduledClassInstanceRepository;

    public Page<Member> getPage(Long id,String date, String name, Integer gender, String email, String phoneNumber, Integer pageNo, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize);
        LocalDateTime dateTime = LocalDateTime.parse(date);
        return memberRepository.getAllByScheduleId(id, dateTime.toLocalDate(), name, gender, email, phoneNumber, pageable);
    }

    public void add(Long scheduledClassId, Long memberId, String bookingTime) throws Exception {
        try {
            LocalDateTime dateTime = LocalDateTime.parse(bookingTime);
            Attendance attendance = attendanceRepository.findByScheduledClassMemberIdDate(scheduledClassId, memberId, dateTime.toLocalDate()).orElse(null);
            Member member = memberRepository.findById(memberId).orElseThrow();
            ScheduledClassInstance scheduledClassInstance = scheduledClassInstanceRepository.findById(scheduledClassId).orElseThrow();
            if(attendance == null){
                attendance = new Attendance();
                attendance.setScheduledClassInstance(scheduledClassInstance);
                attendance.setMember(member);
                attendance.setStatus(1);
                attendance.setBookingTime(scheduledClassInstance.getDate());
                attendance.setCreatedAt(LocalDateTime.now());
                attendance.setUpdatedAt(LocalDateTime.now());
            }
            else {
                attendance.setStatus(1);
            }
            attendanceRepository.save(attendance);
            member.setCoin(member.getCoin() - scheduledClassInstance.getPrice());
            memberRepository.save(member);
        } catch (Exception e) {
            log.error("ERROR|" + e.getMessage(), e);
            throw e;
        }
    }

    public void remove(Long scheduledClassId, Long memberId, String bookingTime) throws Exception {
        try {
            LocalDateTime dateTime = LocalDateTime.parse(bookingTime);
            Attendance attendance = attendanceRepository.findByScheduledClassMemberIdDate(scheduledClassId, memberId, dateTime.toLocalDate()).orElseThrow();
            attendance.setStatus(0);
            attendanceRepository.save(attendance);
            Member member = memberRepository.findById(memberId).orElseThrow();
            ScheduledClassInstance scheduledClassInstance = scheduledClassInstanceRepository.findById(scheduledClassId).orElseThrow();
            member.setCoin(member.getCoin() + scheduledClassInstance.getPrice());
            memberRepository.save(member);
        } catch (Exception e) {
            log.error("ERROR|" + e.getMessage(), e);
            throw e;
        }
    }

    public Integer checkMemberStatus(Long scheduledClassId, Long memberId, String bookingTime) throws Exception {
        LocalDateTime dateTime = LocalDateTime.parse(bookingTime);
        Attendance attendance = attendanceRepository.findByScheduledClassMemberIdDate(scheduledClassId, memberId, dateTime.toLocalDate()).orElseThrow();
        return attendance.getStatus();
    }

}
