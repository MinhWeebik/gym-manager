package com.ringme.cms.repository.gym;

import com.ringme.cms.model.gym.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRepository extends JpaRepository<Member,Long> {

    @Query(value = "SELECT * FROM member m WHERE (LOWER(:name) IS NULL OR CONCAT(m.first_name, ' ', m.last_name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND (:status IS NULL OR m.status = :status) " +
            "AND (:gender IS NULL OR m.gender = :gender) " +
            "AND (LOWER(:email) IS NULL OR m.email LIKE LOWER(CONCAT('%', :email, '%'))) " +
            "AND (LOWER(:phoneNumber) IS NULL OR m.phone_number LIKE LOWER(CONCAT('%', :phoneNumber, '%'))) ",
            countQuery = "SELECT COUNT(*) FROM member m WHERE (LOWER(:name) IS NULL OR CONCAT(m.first_name, ' ', m.last_name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
                    "AND (:status IS NULL OR m.status = :status) " +
                    "AND (:gender IS NULL OR m.gender = :gender) " +
                    "AND (LOWER(:email) IS NULL OR m.email LIKE LOWER(CONCAT('%', :email, '%'))) " +
                    "AND (LOWER(:phoneNumber) IS NULL OR m.phone_number LIKE LOWER(CONCAT('%', :phoneNumber, '%'))) ", nativeQuery = true)
    Page<Member> findAll(@Param("name") String name,
                         @Param("status") Integer status,
                         @Param("gender") Integer gender,
                         @Param("email") String email,
                         @Param("phoneNumber") String phoneNumber, Pageable pageable);

    @Query(value = "SELECT DISTINCT m.* FROM member m " +
            "INNER JOIN member_subscriptions AS ms ON m.id = ms.member_id " +
            "WHERE (LOWER(:name) IS NULL OR CONCAT(m.first_name, ' ', m.last_name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND (:status IS NULL OR m.status = :status) " +
            "AND (:gender IS NULL OR m.gender = :gender) " +
            "AND (LOWER(:email) IS NULL OR m.email LIKE LOWER(CONCAT('%', :email, '%'))) " +
            "AND (LOWER(:phoneNumber) IS NULL OR m.phone_number LIKE LOWER(CONCAT('%', :phoneNumber, '%'))) " +
            "AND ms.status = 1",
            countQuery = "SELECT COUNT(DISTINCT m.id) FROM member m " +
                    "INNER JOIN member_subscriptions AS ms ON m.id = ms.member_id " +
                    "WHERE (LOWER(:name) IS NULL OR CONCAT(m.first_name, ' ', m.last_name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
                    "AND (:status IS NULL OR m.status = :status) " +
                    "AND (:gender IS NULL OR m.gender = :gender) " +
                    "AND (LOWER(:email) IS NULL OR m.email LIKE LOWER(CONCAT('%', :email, '%'))) " +
                    "AND (LOWER(:phoneNumber) IS NULL OR m.phone_number LIKE LOWER(CONCAT('%', :phoneNumber, '%'))) ", nativeQuery = true)
    Page<Member> findAllWithMembership(@Param("name") String name,
                         @Param("status") Integer status,
                         @Param("gender") Integer gender,
                         @Param("email") String email,
                         @Param("phoneNumber") String phoneNumber, Pageable pageable);

    @Query(value = "SELECT * FROM member m " +
            "LEFT JOIN member_subscriptions AS ms ON m.id = ms.member_id " +
            "WHERE (LOWER(:name) IS NULL OR CONCAT(m.first_name, ' ', m.last_name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND (:status IS NULL OR m.status = :status) " +
            "AND (:gender IS NULL OR m.gender = :gender) " +
            "AND (LOWER(:email) IS NULL OR m.email LIKE LOWER(CONCAT('%', :email, '%'))) " +
            "AND (LOWER(:phoneNumber) IS NULL OR m.phone_number LIKE LOWER(CONCAT('%', :phoneNumber, '%'))) " +
            "AND ms.id IS NULL",
            countQuery = "SELECT COUNT(DISTINCT m.id) FROM member m " +
                    "LEFT JOIN member_subscriptions AS ms ON m.id = ms.member_id " +
                    "WHERE (LOWER(:name) IS NULL OR CONCAT(m.first_name, ' ', m.last_name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
                    "AND (:status IS NULL OR m.status = :status) " +
                    "AND (:gender IS NULL OR m.gender = :gender) " +
                    "AND (LOWER(:email) IS NULL OR m.email LIKE LOWER(CONCAT('%', :email, '%'))) " +
                    "AND (LOWER(:phoneNumber) IS NULL OR m.phone_number LIKE LOWER(CONCAT('%', :phoneNumber, '%'))) " +
                    "AND ms.id IS NULL", nativeQuery = true)
    Page<Member> findAllWithNoMembership(@Param("name") String name,
                         @Param("status") Integer status,
                         @Param("gender") Integer gender,
                         @Param("email") String email,
                         @Param("phoneNumber") String phoneNumber, Pageable pageable);
}
