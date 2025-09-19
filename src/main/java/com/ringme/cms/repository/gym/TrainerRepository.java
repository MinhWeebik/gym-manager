package com.ringme.cms.repository.gym;


import com.ringme.cms.model.gym.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainerRepository extends JpaRepository<Trainer, Long> {

    @Query(value = "SELECT c.id AS `id`, CONCAT(c.first_name, ' ', c.last_name) AS `name` " +
            "FROM trainer c " +
            "WHERE c.status = 1 " +
            "AND (:input IS NULL OR (c.id = :input OR CONCAT(c.first_name, ' ', c.last_name) LIKE CONCAT('%', :input, '%'))) " +
            "LIMIT 20", nativeQuery = true)
    List<String[]> ajaxSearchTrainer(@Param("input") String input);
}
