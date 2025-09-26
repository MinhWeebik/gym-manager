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
}
