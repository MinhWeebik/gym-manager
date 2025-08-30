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
        @UniqueField(firstField = "roleName", firstColumn = "role_name", table = "tbl_role")
})
public class RoleFormDto {
    private Long id;
    @NotBlank
    @Size(min = 1, max = 255)
    private String description;
    @NotBlank
    @Size(min = 1, max = 255)
    private String roleName;
}
