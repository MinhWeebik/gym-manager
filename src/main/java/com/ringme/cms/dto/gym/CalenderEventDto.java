package com.ringme.cms.dto.gym;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CalenderEventDto {

    @JsonProperty("id") // Maps eventId to "id" in the JSON
    private String eventId;

    @JsonProperty("title") // Maps eventName to "title"
    private String eventName;

    @JsonProperty("start") // Maps startTime to "start"
    private LocalDateTime startTime;

    @JsonProperty("end") // Maps endTime to "end"
    private LocalDateTime endTime;

    @JsonProperty("backgroundColor") // Maps categoryColor to "backgroundColor"
    private String categoryColor;

    @JsonProperty("borderColor") // Also map it to border color
    private String categoryColorBorder;

    private Boolean recurring = false;

    private Integer type; //0 booking 1 class 2 blocked time 3 available time
    private Boolean overlap = true;
}
