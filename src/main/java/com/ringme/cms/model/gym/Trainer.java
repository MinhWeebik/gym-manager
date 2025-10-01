package com.ringme.cms.model.gym;

import com.ringme.cms.model.sys.User;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "trainer")
public class Trainer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "status")
    private Integer status;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "gender")
    private Integer gender;

    @Column(name = "email")
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "address")
    private String address;

    @Column(name = "bio")
    private String bio;

    @Column(name = "hire_date")
    private LocalDateTime hireDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "city")
    private String city;

    @Column(name = "district")
    private String district;

//    @OneToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "user_id", referencedColumnName = "id")
//    private User user;

    @OneToMany(mappedBy = "trainer", fetch = FetchType.LAZY)
    private List<ScheduledClass> scheduledClasses;

    @OneToMany(mappedBy = "trainer", fetch = FetchType.LAZY)
    private List<Appointment> appointments;

    @OneToMany(mappedBy = "trainer", fetch = FetchType.LAZY)
    private List<BlockedTime> blockedTimes;

    @OneToMany(mappedBy = "trainer", fetch = FetchType.LAZY)
    private List<MemberSubscription> memberSubscriptions;
}
