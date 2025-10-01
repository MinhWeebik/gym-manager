package com.ringme.cms.repository.gym;


import com.ringme.cms.model.gym.Member;
import com.ringme.cms.model.gym.Trainer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query(value = "SELECT * FROM trainer m WHERE (LOWER(:name) IS NULL OR CONCAT(m.first_name, ' ', m.last_name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND ((:status IS NULL AND m.status != 0) OR m.status = :status) " +
            "AND (:gender IS NULL OR m.gender = :gender) " +
            "AND (LOWER(:email) IS NULL OR m.email LIKE LOWER(CONCAT('%', :email, '%'))) " +
            "AND (LOWER(:phoneNumber) IS NULL OR m.phone_number LIKE LOWER(CONCAT('%', :phoneNumber, '%'))) ",
            countQuery = "SELECT COUNT(*) FROM trainer m WHERE (LOWER(:name) IS NULL OR CONCAT(m.first_name, ' ', m.last_name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
                    "AND ((:status IS NULL AND m.status != 0) OR m.status = :status) " +
                    "AND (:gender IS NULL OR m.gender = :gender) " +
                    "AND (LOWER(:email) IS NULL OR m.email LIKE LOWER(CONCAT('%', :email, '%'))) " +
                    "AND (LOWER(:phoneNumber) IS NULL OR m.phone_number LIKE LOWER(CONCAT('%', :phoneNumber, '%'))) ", nativeQuery = true)
    Page<Trainer> findAll(@Param("name") String name,
                         @Param("status") Integer status,
                         @Param("gender") Integer gender,
                         @Param("email") String email,
                         @Param("phoneNumber") String phoneNumber, Pageable pageable);
}
