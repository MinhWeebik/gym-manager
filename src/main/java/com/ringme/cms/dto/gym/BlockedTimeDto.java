package com.ringme.cms.dto.gym;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class BlockedTimeDto {
    private Long id;
    private String startDateStr;
    private String fromStr;
    private String toStr;
    private String repeat;
    private Long trainerId;
    private String endRecurStr;

    private String view;
    private String date;
}
