package com.ringme.cms.repository.gym;

import com.ringme.cms.model.gym.Member;
import com.ringme.cms.model.gym.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    @Query(value = "SELECT * FROM verification_token WHERE token = :token AND expiry_date >= now()", nativeQuery = true)
    Optional<VerificationToken> findByToken(@Param("token") String token);

    @Modifying
    @Query(value = "DELETE FROM verification_token WHERE member_id = :memberId",nativeQuery = true)
    void removeOldToken(@Param("memberId") Long memberId);
}
