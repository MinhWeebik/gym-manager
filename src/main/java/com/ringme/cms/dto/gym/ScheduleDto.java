package com.ringme.cms.dto.gym;

import com.ringme.cms.model.gym.Classes;
import com.ringme.cms.model.gym.Trainer;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class ScheduleDto {
    private Long id;
    private Long classId;
    private Integer capacity;
    private String startDateStr;
    private String fromStr;
    private String toStr;
    private String repeat;
    private Long trainerId;
    private String note;
    private String backgroundColor;
    private String endRecurStr;
    private Integer price;
    private String view;
    private String date;
    private String appointmentInstanceId;

}
