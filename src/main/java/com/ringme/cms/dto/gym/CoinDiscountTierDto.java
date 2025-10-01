package com.ringme.cms.dto.gym;

import lombok.*;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CoinDiscountTierDto {
    private Long id;
    @NotNull
    private Integer minCoins;
    @NotNull
    private BigDecimal bonusPct;
}
