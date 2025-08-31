package com.ringme.cms.repository.gym;

import com.ringme.cms.model.gym.Member;
import com.ringme.cms.model.gym.MemberSubscription;
import jdk.jshell.JShell;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemberSubscriptionRepository extends JpaRepository<MemberSubscription,Long> {

    @Query("SELECT ms FROM MemberSubscription ms JOIN FETCH ms.membership m " +
            "WHERE ms.status = 1 AND m.status = 1 AND ms.member.id = :id")
    List<MemberSubscription> getActiveSubscription(@Param("id") Long id);

    @Query("SELECT ms FROM MemberSubscription ms JOIN FETCH ms.membership m " +
            "WHERE m.status = 1 AND ms.member.id = :id")
    List<MemberSubscription> getAllSubscription(@Param("id") Long id);

    @Query(value = "SELECT m.* FROM member_subscriptions m " +
            "INNER JOIN membership ms ON m.membership_id = ms.id " +
            "WHERE m.status > 0 AND ms.type = :type AND m.member_id = :id",
            nativeQuery = true)
    List<MemberSubscription> findByMemberIdAndType(@Param("id") Long id, @Param("type") Integer type);

    MemberSubscription findByPaypalSubscriptionId(String paypalSubscriptionId);
}
