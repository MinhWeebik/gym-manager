package com.ringme.cms.dto.gym;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class CategoryDto {
    private Long id;
    @NotBlank
    private String name;
    @NotNull
    private BigDecimal taxRate;
    @NotBlank
    private String backgroundColor;
    @NotBlank
    private String textColor;
}
