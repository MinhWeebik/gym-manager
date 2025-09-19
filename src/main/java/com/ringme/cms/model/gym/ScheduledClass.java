package com.ringme.cms.model.gym;

import com.ringme.cms.enums.RecurrenceType;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "scheduled_class")
public class ScheduledClass {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "`from`")
    private LocalTime from;

    @Column(name = "`to`")
    private LocalTime to;

    @Enumerated(EnumType.STRING)
    @Column(name = "`repeat`")
    private RecurrenceType repeat;

    @Column(name = "note")
    private String note;

    @Column(name = "background_color")
    private String backgroundColor;

    @Column(name = "end_recur")
    private LocalDate endRecur;

    @Column(name = "price")
    private Integer price;

    @Column(name = "status")
    private Integer status;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "class_id")
    private Classes classes;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "trainer_id")
    private Trainer trainer;

    @OneToMany(mappedBy = "scheduledClass", fetch = FetchType.LAZY)
    private List<Attendance> attendances;
}
