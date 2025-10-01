package com.ringme.cms.dto.gym;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MemberScheduleDto {
    private LocalDate date;
    private LocalTime from;
    private LocalTime to;
    private Integer status;
    private String name;
    private Integer type; //0 class 1 pt
    private Long trainerId;
}
