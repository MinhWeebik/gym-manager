package com.ringme.cms.repository.gym;

import com.ringme.cms.model.gym.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Payment findByGatewayTransactionId(String gatewayTransactionId);
}
