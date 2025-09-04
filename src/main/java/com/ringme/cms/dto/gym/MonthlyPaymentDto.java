package com.ringme.cms.dto.gym;

import lombok.*;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MonthlyPaymentDto {
    String paymentMonth;
    BigDecimal netTotalAmount;
}
