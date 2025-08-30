package com.ringme.cms.dto.sys;

import com.ringme.cms.validationfield.UniqueField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@UniqueField.List({
        @UniqueField(firstField = "des", firstColumn = "des", table = "tbl_router"),
        @UniqueField(firstField = "routerLink", firstColumn = "router_link", table = "tbl_router")
})
public class RouterFormDto {
    private Long id;
    @NotBlank
    @Size(min = 1, max = 255)
    private String des;
    @NotBlank
    @Size(min = 1, max = 255)
    private String routerLink;
}
