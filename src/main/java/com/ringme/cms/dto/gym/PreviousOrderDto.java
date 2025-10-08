package com.ringme.cms.dto.gym;

import com.ringme.cms.model.gym.Product;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PreviousOrderDto {
    private List<ProductDto> products;
    private BigDecimal tax;
    private BigDecimal total;
}
