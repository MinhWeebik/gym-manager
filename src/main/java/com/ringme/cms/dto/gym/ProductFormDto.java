package com.ringme.cms.dto.gym;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProductFormDto {
    private Long id;
    private Long categoryId;
    @NotBlank
    private String name;
    private String description;
    @NotNull
    private BigDecimal price;
    @NotBlank
    private String backgroundColor;
    @NotBlank
    private String textColor;
}
