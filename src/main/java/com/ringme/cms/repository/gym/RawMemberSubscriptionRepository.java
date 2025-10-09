package com.ringme.cms.repository.gym;

import com.ringme.cms.model.gym.RawMemberSubscriptionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface RawMemberSubscriptionRepository extends JpaRepository<RawMemberSubscriptionLog, Long> {
    @Query(value = "SELECT COUNT(*) FROM raw_membersubscription_log WHERE MONTH(sign_up_date) = MONTH(CURDATE())", nativeQuery = true)
    Integer getThisMonthSoldData();

    @Query(value = "SELECT SUM(m.price) AS total FROM raw_membersubscription_log rml INNER JOIN member_subscriptions ms ON ms.id = rml.member_subscription_id \n" +
            "INNER JOIN membership m ON m.id = ms.membership_id AND MONTH(rml.sign_up_date) = MONTH(CURDATE()) AND m.id = :membershipId", nativeQuery = true)
    BigDecimal getMonthlyDonutData(@Param("membershipId") Long membershipId);

    @Query(value = "SELECT SUM(m.price) AS total FROM raw_membersubscription_log rml INNER JOIN member_subscriptions ms ON ms.id = rml.member_subscription_id \n" +
            "INNER JOIN membership m ON m.id = ms.membership_id AND YEAR(rml.sign_up_date) = YEAR(CURDATE()) AND m.id = :membershipId", nativeQuery = true)
    BigDecimal getYearlyDonutData(@Param("membershipId") Long membershipId);
}
