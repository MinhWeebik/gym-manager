package com.ringme.cms.repository.gym;

import com.ringme.cms.model.gym.RawCheckInLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RawCheckInLogRepository extends JpaRepository<RawCheckInLog, Long> {

    List<RawCheckInLog> findByMemberId(Long memberId);
}
