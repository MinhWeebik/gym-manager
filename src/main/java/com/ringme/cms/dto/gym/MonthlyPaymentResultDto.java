package com.ringme.cms.dto.gym;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MonthlyPaymentResultDto {
    private String label;
    private List<BigDecimal> data;
    private String backgroundColor;
}
