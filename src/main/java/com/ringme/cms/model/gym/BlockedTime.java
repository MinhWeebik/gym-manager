package com.ringme.cms.model.gym;

import com.ringme.cms.enums.RecurrenceType;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@ToString
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "blocked_time")
public class BlockedTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "`from`")
    private LocalTime from;

    @Column(name = "`to`")
    private LocalTime to;

    @Enumerated(EnumType.STRING)
    @Column(name = "`repeat`")
    private RecurrenceType repeat;

    @Column(name = "end_recur")
    private LocalDate endRecur;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "trainer_id")
    private Trainer trainer;
}
