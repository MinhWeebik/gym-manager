package com.ringme.cms.model.gym;

import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "scheduled_class_instance")
public class ScheduledClassInstance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "`from`")
    private LocalTime from;

    @Column(name = "`to`")
    private LocalTime to;

    @Column(name = "status")
    private Integer status;

    @Column(name = "is_repeat")
    private Integer isRepeat;

    @Column(name = "original_time")
    private LocalDateTime originalTime;

    @Column(name = "price")
    private Integer price;

    @Column(name = "capacity")
    private Integer capacity;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "scheduled_class_id")
    private ScheduledClass scheduledClass;

    @OneToMany(mappedBy = "scheduledClassInstance", fetch = FetchType.LAZY)
    private List<Attendance> attendances;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "trainer_id")
    private Trainer trainer;
}
