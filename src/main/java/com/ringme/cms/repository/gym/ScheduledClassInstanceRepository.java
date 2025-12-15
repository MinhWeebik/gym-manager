package com.ringme.cms.repository.gym;

import com.ringme.cms.model.gym.AppointmentInstance;
import com.ringme.cms.model.gym.ScheduledClassInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public interface ScheduledClassInstanceRepository extends JpaRepository<ScheduledClassInstance, Long> {
    @Query(value = "SELECT ai.* FROM scheduled_class_instance ai INNER JOIN scheduled_class a ON a.id = ai.scheduled_class_id  " +
            "WHERE ai.date BETWEEN :startDate AND :endDate AND (:trainerId IS NULL OR a.trainer_id = :trainerId) and ai.status!=0", nativeQuery = true)
    List<ScheduledClassInstance> getAllInstance(@Param("startDate") LocalDate startDate,
                                                        @Param("endDate") LocalDate endDate,
                                                        @Param("trainerId") Long trainerId);

    @Query(value = "SELECT * FROM scheduled_class_instance WHERE scheduled_class_id = :id AND original_time > :date", nativeQuery = true)
    List<ScheduledClassInstance> findByAppointmentIdAndDate(@Param("id") Long id,
                                                         @Param("date") LocalDateTime date);

    @Query(value = "SELECT id FROM scheduled_class_instance WHERE scheduled_class_id = :id AND original_time >= :date", nativeQuery = true)
    List<Long> findIdByAppointmentIdAndDate(@Param("id") Long id,
                                            @Param("date") LocalDateTime date);

    @Query(value = "SELECT *\n" +
            "FROM scheduled_class_instance\n" +
            "WHERE scheduled_class_id = :id\n" +
            "  AND (\n" +
            "        (`date` = :date AND `from` >= :time) " +
            "        OR (`date` > :date) " +
            "      )\n" +
            "ORDER BY original_time ASC;\n", nativeQuery = true)
    List<ScheduledClassInstance> findByScheduleIdAndThreshold(@Param("id") Long id,
                                                              @Param("date") LocalDate date,
                                                              @Param("time") LocalTime time);

    @Query(value = "SELECT id\n" +
            "FROM scheduled_class_instance\n" +
            "WHERE scheduled_class_id = :id\n" +
            "  AND (\n" +
            "        (`date` = :date AND `from` >= :time) " +
            "        OR (`date` > :date) " +
            "      )\n" +
            "ORDER BY original_time ASC;\n", nativeQuery = true)
    List<Long> findIdByScheduleIdAndThreshold(@Param("id") Long id,
                                                              @Param("date") LocalDate date,
                                                              @Param("time") LocalTime time);

    @Modifying
    @Query(value = "UPDATE scheduled_class_instance SET status = 0 WHERE id IN :ids", nativeQuery = true)
    void bulkDelete(@Param("ids") List<Long> ids);

    @Modifying
    @Query(value = "UPDATE scheduled_class_instance SET trainer_id = :trainerId WHERE id IN :ids", nativeQuery = true)
    void bulkUpdateTrainer(@Param("ids") List<Long> ids,
                    @Param("trainerId") Long trainerId);

    @Modifying
    @Query(value = "UPDATE scheduled_class_instance SET price = :price WHERE id IN :ids", nativeQuery = true)
    void bulkUpdatePrice(@Param("ids") List<Long> ids,
                           @Param("price") int price);

    @Modifying
    @Query(value = "UPDATE scheduled_class_instance SET capacity = :capacity WHERE id IN :ids", nativeQuery = true)
    void bulkUpdateCapacity(@Param("ids") List<Long> ids,
                           @Param("capacity") int capacity);

    @Modifying
    @Query(value = "DELETE FROM scheduled_class_instance WHERE id IN :ids", nativeQuery = true)
    void bulkDeleteHard(@Param("ids") List<Long> ids);
}
