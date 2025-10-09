package com.ringme.cms.model.gym;

import com.ringme.cms.enums.EventType;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@ToString
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "raw_membersubscription_log")
public class RawMemberSubscriptionLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_subscription_id")
    private Long memberSubscriptionId;

    @Column(name = "payment_id")
    private Long paymentId;

    @Column(name = "sign_up_date")
    private LocalDateTime signUpDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type")
    private EventType eventType;
}
