package com.ringme.cms.dto.gym;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RecalculateDto {
    private Long id;
    private Integer type;
    private String dateString;
}
