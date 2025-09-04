package com.ringme.cms.repository.gym;

import com.ringme.cms.model.gym.Membership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MembershipRepository extends JpaRepository<Membership, Long> {
    @Query(value = "SELECT c.id AS `id`, c.name AS `name`, c.price AS `price`, " +
            "    c.duration AS `duration`, c.type AS `type` " +
            "FROM membership c " +
            "WHERE c.status = 1 " +
            "AND (:input IS NULL OR (c.id = :input OR c.name LIKE CONCAT('%', :input, '%'))) " +
            "AND (:type IS NULL OR c.type = :type) " +
            "LIMIT 20", nativeQuery = true)
    List<String[]> ajaxSearchMembership(@Param("input") String input,
                                        @Param("type") Integer type);
}
