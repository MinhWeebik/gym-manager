package com.ringme.cms.model.gym;

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
@Table(name = "member")
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "uuid")
    private String uuid;

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

    @Column(name = "home_number")
    private String homeNumber;

    @Column(name = "work_number")
    private String workNumber;

    @Column(name = "city")
    private String city;

    @Column(name = "district")
    private String district;

    @Column(name = "note")
    private String note;

    @Column(name = "last_checked_in")
    private LocalDateTime lastCheckedIn;

    @Column(name = "coin")
    private Integer coin;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
    private List<Payment> payments;

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
    private List<MemberSubscription> memberSubscriptions;

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
    private List<Attendance> attendances;

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
    private List<Appointment> appointments;
}
