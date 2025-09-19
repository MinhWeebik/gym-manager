package com.ringme.cms.model.gym;

import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@ToString
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "member_subscriptions")
public class MemberSubscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "start_at")
    private LocalDate startAt;

    @Column(name = "end_at")
    private LocalDate endAt;

    @Column(name = "status") //-2 FROZEN, -1 CANCELLED, 0 EXPIRED, 1 ACTIVE, 2 PENDING, 3 RENEWAL_FAILED
    private Integer status;

    @Column(name = "number_of_visit")
    private Integer numberOfVisit;

    @Column(name = "isRecurring")
    private Integer isRecurring;

    @Column(name = "paypal_subscription_id")
    private String paypalSubscriptionId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "trainer_id")
    private Trainer trainer;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "membership_id")
    private Membership membership;


}
