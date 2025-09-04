package com.ringme.cms.model.gym;

import com.ringme.cms.dto.gym.MonthlyPaymentDto;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@SqlResultSetMapping(
        name = "MonthlyPaymentMapping",
        classes = @ConstructorResult(
                targetClass = MonthlyPaymentDto.class,
                columns = {
                        @ColumnResult(name = "payment_month", type = String.class),
                        @ColumnResult(name = "net_total_amount", type = BigDecimal.class)
                }
        )
)

@NamedNativeQuery(
        name = "Payment.findNetTotalAmountByMonthForCurrentYear",
        query = "SELECT " +
                "    DATE_FORMAT(transaction_date, '%Y-%m') AS payment_month, " +
                "    SUM(CASE WHEN type = 1 THEN amount WHEN type = 0 THEN -amount ELSE 0 END) AS net_total_amount " +
                "FROM payment " +
                "WHERE YEAR(transaction_date) = YEAR(CURDATE()) AND type IN (0, 1) AND status = 1 AND member_id = :memberId " +
                "GROUP BY payment_month ORDER BY payment_month ASC",
        resultSetMapping = "MonthlyPaymentMapping"
)
@Getter
@Setter
@NoArgsConstructor
@ToString
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "payment")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "description")
    private String description;

    @Column(name = "amount")
    private Long amount;

    @Column(name = "status") //-1 failed, 0 inactive, 1 success, 2 pending
    private Integer status;

    @Column(name = "payment_gateway")
    private String paymentGateway;

    @Column(name = "gateway_transaction_id")
    private String gatewayTransactionId;

    @Column(name = "type") //0 refund, 1 purchase
    private Integer type;

    @Column(name = "payment_url")
    private String paymentUrl;

    @Column(name = "transaction_date")
    private LocalDateTime transactionDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;
}
