package com.ringme.cms.repository.gym;

import com.ringme.cms.model.gym.Member;
import com.ringme.cms.model.gym.MemberSubscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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

    @Query(value = "SELECT m.id FROM member m INNER JOIN member_subscriptions ms ON ms.member_id = m.id WHERE ms.id = :id",nativeQuery = true)
    Long findIdByMemberSubscriptionId(@Param("id") Long id);

    Optional<Member> findByUuid(String uuid);

    @Query(value = "SELECT m.* " +
            "FROM attendance a " +
            "INNER JOIN member m ON m.id = a.member_id " +
            "WHERE a.scheduled_class_instance_id = :id " +
            "  AND a.booking_time = :date " +
            "  AND (LOWER(:name) IS NULL OR CONCAT(m.first_name, ' ', m.last_name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "  AND (:gender IS NULL OR m.gender = :gender) " +
            "  AND (LOWER(:email) IS NULL OR m.email LIKE LOWER(CONCAT('%', :email, '%'))) " +
            "  AND (LOWER(:phoneNumber) IS NULL OR m.phone_number LIKE LOWER(CONCAT('%', :phoneNumber, '%'))) " +
            "  AND a.status != 0 " +
            "ORDER BY a.created_at", nativeQuery = true)
    Page<Member> getAllByScheduleId(@Param("id") Long id,
                                    @Param("date") LocalDate date,
                                    @Param("name") String name,
                                    @Param("gender") Integer gender,
                                    @Param("email") String email,
                                    @Param("phoneNumber") String phoneNumber, Pageable pageable);

    @Query(value = "SELECT * FROM member m WHERE (LOWER(:name) IS NULL OR CONCAT(m.first_name, ' ', m.last_name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND (:gender IS NULL OR m.gender = :gender) " +
            "AND (LOWER(:email) IS NULL OR m.email LIKE LOWER(CONCAT('%', :email, '%'))) " +
            "AND (LOWER(:phoneNumber) IS NULL OR m.phone_number LIKE LOWER(CONCAT('%', :phoneNumber, '%'))) " +
            "AND m.status = 1 AND (:isEmpty = true OR m.id NOT IN (:exceptIds)) " +
            "ORDER BY m.created_at",
            countQuery = "SELECT COUNT(*) FROM member m WHERE (LOWER(:name) IS NULL OR CONCAT(m.first_name, ' ', m.last_name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
                    "AND (:gender IS NULL OR m.gender = :gender) " +
                    "AND (LOWER(:email) IS NULL OR m.email LIKE LOWER(CONCAT('%', :email, '%'))) " +
                    "AND (LOWER(:phoneNumber) IS NULL OR m.phone_number LIKE LOWER(CONCAT('%', :phoneNumber, '%'))) " +
                    "AND m.status = 1 AND (:isEmpty = true OR m.id NOT IN (:exceptIds))", nativeQuery = true)
    Page<Member> search(@Param("name") String name,
                         @Param("gender") Integer gender,
                         @Param("email") String email,
                         @Param("phoneNumber") String phoneNumber,
                        @Param("isEmpty") boolean isEmpty,
                         @Param("exceptIds") List<Long> exceptIds,
                        Pageable pageable);

    @Query(
            value = """
            SELECT 
                c.id AS id, 
                CONCAT(c.first_name, ' ', c.last_name) AS name,
                (SUM(m.total_visit) - SUM(ms.number_of_visit)) AS visits_left 
            FROM 
                member c
            INNER JOIN 
                member_subscriptions ms ON ms.member_id = c.id
            INNER JOIN 
                membership m ON m.id = ms.membership_id
            WHERE 
                c.status = 1
                AND m.type = 1
                AND ms.status = 1
                AND ms.trainer_id = :trainerId
                AND (
                    :input IS NULL 
                    OR c.id LIKE :input
                    OR CONCAT(c.first_name, ' ', c.last_name) LIKE CONCAT('%', :input, '%')
                )
            GROUP BY
                c.id, c.first_name, c.last_name
            HAVING
                visits_left > 0
        """,
            nativeQuery = true
    )
    List<String[]> ajaxSearchMember(@Param("input") String input, @Param("trainerId") Long trainerId);
        @Query(
                value = """
            SELECT 
                (SUM(m.total_visit) - SUM(ms.number_of_visit)) AS visits_left 
            FROM 
                member c
            INNER JOIN 
                member_subscriptions ms ON ms.member_id = c.id
            INNER JOIN 
                membership m ON m.id = ms.membership_id
            WHERE 
                c.status = 1
                AND m.type = 1
                AND ms.status = 1
                AND ms.trainer_id = :trainerId
                AND c.id = :memberId
            GROUP BY
                c.id
        """,
                nativeQuery = true
        )
        Optional<Integer> findVisitsLeftForMember(
                @Param("memberId") Long memberId,
                @Param("trainerId") Long trainerId
        );

    @Query(value = "SELECT COUNT(*) FROM member WHERE status = 1", nativeQuery = true)
    Integer getMemberAmount();

    @Modifying
    @Query(value = "UPDATE member m " +
            "INNER JOIN ( " +
            "    SELECT a.member_id, SUM(sci.price) as total_price " +
            "    FROM attendance a " +
            "    INNER JOIN scheduled_class_instance sci ON sci.id = a.scheduled_class_instance_id " +
            "    WHERE sci.id IN :ids " +
            "    GROUP BY a.member_id " +
            ") AS refund_data ON m.id = refund_data.member_id " +
            "SET m.coin = m.coin + refund_data.total_price", nativeQuery = true)
    void refundCoin(@Param("ids") List<Long> ids);

    @Modifying
    @Query(value = "UPDATE member m INNER JOIN attendance a ON a.member_id = m.id SET m.coin = m.coin + :coin WHERE a.scheduled_class_instance_id IN :ids", nativeQuery = true)
    void bulkUpdateCoin(@Param("ids") List<Long> ids,
                    @Param("coin") Integer coin);
}
