package com.ringme.cms.repository.gym;

import com.ringme.cms.model.gym.AppointmentInstance;
import com.ringme.cms.model.gym.ScheduledClass;
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
}
