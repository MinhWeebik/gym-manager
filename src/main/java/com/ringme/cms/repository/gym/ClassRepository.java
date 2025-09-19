package com.ringme.cms.repository.gym;

import com.ringme.cms.model.gym.Classes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassRepository extends JpaRepository<Classes, Long> {

    @Query(value = "SELECT c.id AS `id`, c.name AS `name` " +
            "FROM classes c " +
            "WHERE c.status = 1 " +
            "AND (:input IS NULL OR (c.id = :input OR c.name LIKE CONCAT('%', :input, '%'))) " +
            "LIMIT 20", nativeQuery = true)
    List<String[]> ajaxSearchClass(@Param("input") String input);
}
