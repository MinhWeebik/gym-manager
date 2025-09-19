package com.ringme.cms.dto.gym;

import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MemberAttendanceDto {
    private Long id;
    private String imageUrl;
    private String firstName;
    private String lastName;
    private Integer gender;
    private String email;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private Integer status;
}
