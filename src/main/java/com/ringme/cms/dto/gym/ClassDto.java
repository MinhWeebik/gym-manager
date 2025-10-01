package com.ringme.cms.dto.gym;

import lombok.*;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ClassDto {
    private Long id;
    @NotBlank
    private String name;
    @NotBlank
    private String description;
}
