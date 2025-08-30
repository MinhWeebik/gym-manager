package com.ringme.cms.dto.sys;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MenuFormDto {
    private Long id;
    @NotBlank
    @Size(min = 1, max = 255)
    private String nameVn;
    @NotBlank
    @Size(min = 1, max = 255)
    private String nameEn;
    @NotNull
    @Min(1)
    private Integer orderNum;
    @NotNull
    @Min(0)
    private Long iconId;
    private Long parentNameId;
    @NotNull
    @Min(0)
    private Long routerId;
}
