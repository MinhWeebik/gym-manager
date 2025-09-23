package com.ringme.cms.dto.gym;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class AppointmentDto {
    private Long id;
    private Long memberId;
    private String startDateStr;
    private String fromStr;
    private String toStr;
    private String repeat;
    private Long trainerId;
    private String note;
    private String backgroundColor;
    private String endRecurStr;
    private String view;
    private String date;
    private Long appointmentInstanceId;
}
