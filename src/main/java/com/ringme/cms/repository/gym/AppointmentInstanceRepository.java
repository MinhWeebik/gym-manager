package com.ringme.cms.repository.gym;

import com.ringme.cms.model.gym.AppointmentInstance;
import com.ringme.cms.model.gym.ScheduledClass;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface AppointmentInstanceRepository extends JpaRepository<AppointmentInstance, Long> {

    @Query(value = "SELECT ai.* FROM appointment_instance ai INNER JOIN appointment a ON a.id = ai.appointment_id  " +
            "WHERE ai.date BETWEEN :startDate AND :endDate AND (:trainerId IS NULL OR a.trainer_id = :trainerId) and ai.status!=0", nativeQuery = true)
    List<AppointmentInstance> getAllAppointmentInstance(@Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate,
                                          @Param("trainerId") Long trainerId);

    @Query(value = "SELECT * FROM appointment_instance WHERE appointment_id = :id AND original_time > :date", nativeQuery = true)
    List<AppointmentInstance> findByAppointmentIdAndDate(@Param("id") Long id,
                                                         @Param("date") LocalDateTime date);

    @Query(value = "SELECT *\n" +
            "FROM appointment_instance\n" +
            "WHERE appointment_id = :id\n" +
            "  AND (\n" +
            "        (`date` = :date AND `from` >= :time) " +
            "        OR (`date` > :date) " +
            "      )\n" +
            "ORDER BY original_time ASC;\n", nativeQuery = true)
    List<AppointmentInstance> findByAppointmentIdAndThreshold(@Param("id") Long id,
                                                              @Param("date") LocalDate date,
                                                              @Param("time") LocalTime time);

    @Query(value = "SELECT a.* FROM appointment_instance a INNER JOIN appointment ap ON ap.id = a.appointment_id " +
            "WHERE ap.member_id = :id AND ((:status IS NULL AND a.status != 0) OR a.status = :status) " +
            "ORDER BY a.date DESC, a.from DESC",
            countQuery = "SELECT COUNT(*) FROM appointment_instance a INNER JOIN appointment ap ON ap.id = a.appointment_id " +
                    "WHERE ap.member_id = :id AND ((:status IS NULL AND a.status != 0) OR a.status = :status) " +
                    "ORDER BY a.date DESC, a.from DESC", nativeQuery = true)
    Page<AppointmentInstance> getForUserDetail(@Param("status") Integer status,
                                               @Param("id") Long id,
                                               Pageable pageable);

    @Query(value = "SELECT COUNT(*) FROM appointment_instance a INNER JOIN appointment ap ON ap.id = a.appointment_id " +
            "WHERE ap.member_id = :id AND ((:status IS NULL AND a.status != 0) OR a.status = :status) ", nativeQuery = true)
    Integer getTotalRecord(@Param("status") Integer status,
                           @Param("id") Long id);
}
