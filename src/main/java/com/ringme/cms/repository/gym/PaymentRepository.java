package com.ringme.cms.repository.gym;

import com.ringme.cms.dto.gym.MonthlyPaymentDto;
import com.ringme.cms.model.gym.Member;
import com.ringme.cms.model.gym.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query(value = "SELECT * FROM payment WHERE status != 0 AND member_id = :id " +
            "AND (:paymentDescription is null or description LIKE CONCAT('%', :paymentDescription, '%')) " +
            "AND (:paymentGateway is null or payment_gateway LIKE CONCAT('%', :paymentGateway, '%')) "+
            "AND ((:paymentStatus is null and status != -1) or status = :paymentStatus) " +
            "AND (:paymentType is null or type = :paymentType) " +
            "ORDER BY transaction_date desc",
            countQuery = "SELECT count(*) FROM payment WHERE status != 0 AND member_id = :id " +
                    "AND (:paymentDescription is null or description LIKE CONCAT('%', :paymentDescription, '%')) " +
                    "AND (:paymentGateway is null or payment_gateway LIKE CONCAT('%', :paymentGateway, '%')) "+
                    "AND ((:paymentStatus is null and status != -1) or status = :paymentStatus) " +
                    "AND (:paymentType is null or type = :paymentType) ", nativeQuery = true)
    Page<Payment> getAll(@Param("id") Long id,
                         @Param("paymentDescription") String paymentDescription,
                         @Param("paymentGateway") String paymentGateway,
                         @Param("paymentStatus") Integer paymentStatus,
                         @Param("paymentType") Integer paymentType, Pageable pageable);
}
