package com.ringme.cms.dto.gym;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MembershipDto {
    Long id;
    @NotBlank
    String name;
    @NotNull
    Integer price;
    @NotNull
    Integer duration;
    @NotBlank
    String description;
    Integer totalVisit;
    @NotNull
    Integer type;
}
