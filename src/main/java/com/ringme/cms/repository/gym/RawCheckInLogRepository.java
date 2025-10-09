package com.ringme.cms.repository.gym;

import com.ringme.cms.model.gym.RawCheckInLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RawCheckInLogRepository extends JpaRepository<RawCheckInLog, Long> {

    @Query(value = "SELECT * FROM raw_check_in_log WHERE member_id = :memberId ORDER BY created_at DESC LIMIT 20", nativeQuery = true)
    List<RawCheckInLog> getLogByMemberId(@Param("memberId") Long memberId);

    @Query(value = "SELECT COUNT(DISTINCT member_id) AS visit_count\n" +
            "FROM raw_check_in_log\n" +
            "WHERE created_at >= CURDATE()\n" +
            "  AND created_at < CURDATE() + INTERVAL 1 DAY;\n", nativeQuery = true)
    Integer getTodayVisit();

    @Query(value  = "SELECT COUNT(*) AS visit_count " +
            "FROM raw_check_in_log " +
            "WHERE YEAR(created_at) = YEAR(CURDATE()) " +
            "  AND MONTH(created_at) = :month ", nativeQuery = true)
    Integer getDataByMonth(@Param("month") Integer month);
}
