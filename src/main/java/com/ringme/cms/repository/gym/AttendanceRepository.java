package com.ringme.cms.repository.gym;

import com.ringme.cms.model.gym.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    @Query(value = "SELECT COUNT(*) FROM attendance WHERE scheduled_class_id = :id AND booking_time = :date AND status != 0", nativeQuery = true)
    Integer getNumberOfAttendance(@Param("id") Long id,
                                  @Param("date") LocalDate date);

    @Query(value = "SELECT * FROM attendance WHERE scheduled_class_id = :id AND (:date IS NULL OR booking_time = :date) AND status != 0", nativeQuery = true)
    List<Attendance> findByScheduleIdAndDate(@Param("id") Long id,
                                             @Param("date") LocalDate date);

    @Query(value = "SELECT * FROM attendance WHERE scheduled_class_id = :id AND booking_time >= :date AND status != 0", nativeQuery = true)
    List<Attendance> findByScheduleIdAndDateLimit(@Param("id") Long id,
                                             @Param("date") LocalDate date);

    @Query(value = "SELECT member_id FROM attendance WHERE scheduled_class_id = :id AND booking_time = :date AND status != 0", nativeQuery = true)
    Set<Long> findMemberIdsFromAttendanceId(@Param("id") Long id, @Param("date") LocalDate date);

    @Query(value = "SELECT * FROM attendance WHERE scheduled_class_id = :scheduledClassId AND booking_time = :date AND member_id = :memberId ", nativeQuery = true)
    Optional<Attendance> findByScheduledClassMemberIdDate(@Param("scheduledClassId") Long scheduledClassId,
                                      @Param("memberId") Long memberId,
                                      @Param("date")  LocalDate date);
}
