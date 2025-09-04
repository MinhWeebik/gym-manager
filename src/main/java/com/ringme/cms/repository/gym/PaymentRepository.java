package com.ringme.cms.repository.gym;

import com.ringme.cms.dto.gym.MonthlyPaymentDto;
import com.ringme.cms.model.gym.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Payment findByGatewayTransactionId(String gatewayTransactionId);

    @Query(name = "Payment.findNetTotalAmountByMonthForCurrentYear", nativeQuery = true)
    List<MonthlyPaymentDto> findNetTotalAmountByMonthForCurrentYear(@Param("memberId") Long memberId);
}
