package com.ringme.cms.dto.gym;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MonthlyVisitResultDto {
    private String label;
    private List<Integer> data;
    private String backgroundColor;
}
