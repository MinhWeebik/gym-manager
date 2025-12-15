package com.ringme.cms.repository.gym;

import com.ringme.cms.model.gym.Attendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    @Query(value = "SELECT COUNT(*) FROM attendance WHERE scheduled_class_instance_id = :id AND status != 0", nativeQuery = true)
    Integer getNumberOfAttendance(@Param("id") Long id);

    @Query(value = "SELECT * FROM attendance WHERE scheduled_class_instance_id = :id AND (:date IS NULL OR booking_time = :date) AND status != 0", nativeQuery = true)
    List<Attendance> findByScheduleIdAndDate(@Param("id") Long id,
                                             @Param("date") LocalDate date);

    @Query(value = "SELECT * FROM attendance WHERE scheduled_class_instance_id = :id AND booking_time >= :date AND status != 0", nativeQuery = true)
    List<Attendance> findByScheduleIdAndDateLimit(@Param("id") Long id,
                                             @Param("date") LocalDate date);

    @Query(value = "SELECT member_id FROM attendance WHERE scheduled_class_instance_id = :id AND booking_time = :date AND status != 0", nativeQuery = true)
    Set<Long> findMemberIdsFromAttendanceId(@Param("id") Long id, @Param("date") LocalDate date);

    @Query(value = "SELECT * FROM attendance WHERE scheduled_class_instance_id = :scheduledClassId AND booking_time = :date AND member_id = :memberId ", nativeQuery = true)
    Optional<Attendance> findByScheduledClassMemberIdDate(@Param("scheduledClassId") Long scheduledClassId,
                                      @Param("memberId") Long memberId,
                                      @Param("date")  LocalDate date);

    @Query(value = "SELECT * FROM attendance a INNER JOIN scheduled_class s ON s.id = a.scheduled_class_id WHERE class_id = :id AND a.status = 1", nativeQuery = true)
    List<Attendance> findByClassId(@Param("id") Long id);

    @Query(value = "SELECT a.* FROM attendance a INNER JOIN scheduled_class_instance sc ON sc.id = a.scheduled_class_instance_id WHERE a.member_id = :id " +
            "AND ((:status IS NULL AND a.status != 0) OR a.status = :status) " +
            "ORDER BY a.booking_time DESC, sc.from DESC",
            countQuery = "SELECT count(*) FROM attendance a INNER JOIN scheduled_class_instance sc ON sc.id = a.scheduled_class_instance_id WHERE a.member_id = :id " +
                    "AND ((:status IS NULL AND a.status != 0) OR a.status = :status) " +
                    "ORDER BY a.booking_time DESC, sc.from DESC", nativeQuery = true)
    Page<Attendance> getForUserDetail(@Param("status") Integer status,
                                      @Param("id") Long id, Pageable pageable);

    @Query(value = "SELECT count(*) FROM attendance a WHERE a.member_id = :id " +
            "AND ((:status IS NULL AND a.status != 0) OR a.status = :status) ", nativeQuery = true)
    Integer getTotalRecord(@Param("status") Integer status,
                           @Param("id") Long id);

    @Query(value = "SELECT * " +
            "FROM attendance a " +
            "WHERE a.status = 1 " +
            "  AND a.booking_time = CURDATE()", nativeQuery = true)
    List<Attendance> getTodayClass();

    @Query(value = "SELECT * FROM attendance a WHERE a.scheduled_class_instance_id = :id AND a.status = 1", nativeQuery = true)
    List<Attendance> findByScheduledClassInstanceId(@Param("id") Long id);

    @Modifying
    @Query(value = "DELETE FROM attendance WHERE scheduled_class_instance_id IN :ids", nativeQuery = true)
    void bulkDelete(@Param("ids") List<Long> ids);

}
