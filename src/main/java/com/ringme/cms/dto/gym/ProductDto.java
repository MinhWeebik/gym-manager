package com.ringme.cms.dto.gym;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProductDto {
    private Long id;
    private String name;
    private Integer price;
    private Integer amount;
}
