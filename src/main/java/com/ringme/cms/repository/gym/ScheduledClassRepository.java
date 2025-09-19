package com.ringme.cms.repository.gym;

import com.ringme.cms.model.gym.ScheduledClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ScheduledClassRepository extends JpaRepository<ScheduledClass, Long> {

    @Query(value = "SELECT * FROM scheduled_class sc WHERE (sc.date between :startDate and :endDate) " +
            "AND sc.repeat = 'NONE' AND sc.status = 1 AND (:trainerId IS NULL OR sc.trainer_id = :trainerId)", nativeQuery = true)
    List<ScheduledClass> getAllNoneRepeat(@Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate,
                                          @Param("trainerId") Long trainerId);

    @Query(value = "SELECT * FROM scheduled_class sc WHERE sc.date <= :endDate " +
            "AND (sc.end_recur IS null OR sc.end_recur >= :startDate) " +
            "AND sc.repeat != 'NONE' AND sc.status = 1 AND (:trainerId IS NULL OR sc.trainer_id = :trainerId)", nativeQuery = true)
    List<ScheduledClass> getAllRepeat(@Param("startDate") LocalDate startDate,
                                      @Param("endDate") LocalDate endDate,
                                      @Param("trainerId") Long trainerId);
}
