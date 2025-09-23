package com.ringme.cms.model.gym;

import com.ringme.cms.enums.RecurrenceType;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@ToString
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "appointment_instance")
public class AppointmentInstance {
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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;
}
