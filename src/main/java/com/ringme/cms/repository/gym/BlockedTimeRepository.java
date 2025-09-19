package com.ringme.cms.repository.gym;

import com.ringme.cms.model.gym.BlockedTime;
import com.ringme.cms.model.gym.ScheduledClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BlockedTimeRepository extends JpaRepository<BlockedTime,Long> {
    @Query(value = "SELECT * FROM blocked_time bt WHERE (bt.date between :startDate and :endDate) " +
            "AND bt.repeat = 'NONE' " +
            "AND bt.trainer_id IS NULL", nativeQuery = true)
    List<BlockedTime> getAllNonRepeate(@Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate);

    @Query(value = "SELECT * FROM blocked_time bt WHERE bt.date <= :endDate " +
            "AND (bt.end_recur IS null OR bt.end_recur >= :startDate) " +
            "AND bt.repeat != 'NONE' " +
            "AND bt.trainer_id IS NULL", nativeQuery = true)
    List<BlockedTime> getAllRepeated(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
