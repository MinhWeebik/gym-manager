package com.ringme.cms.repository.gym;

import com.ringme.cms.model.gym.Appointment;
import com.ringme.cms.model.gym.ScheduledClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment,Long> {

    @Query(value = "SELECT * FROM appointment ap WHERE (ap.date between :startDate and :endDate) " +
            "AND ap.repeat = 'NONE' AND ap.status != 0 AND ap.trainer_id = :trainerId", nativeQuery = true)
    List<Appointment> getAllNoneRepeat(@Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate,
                                          @Param("trainerId") Long trainerId);

    @Query(value = "SELECT * FROM appointment ap WHERE ap.date <= :endDate " +
            "AND (ap.end_recur IS null OR ap.end_recur >= :startDate) " +
            "AND ap.repeat != 'NONE' AND ap.status != 0 AND ap.trainer_id = :trainerId", nativeQuery = true)
    List<Appointment> getAllRepeat(@Param("startDate") LocalDate startDate,
                                      @Param("endDate") LocalDate endDate,
                                      @Param("trainerId") Long trainerId);

}
