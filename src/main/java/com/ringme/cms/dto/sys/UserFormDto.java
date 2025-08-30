package com.ringme.cms.dto.sys;

import com.ringme.cms.validationfield.FieldMatch;
import com.ringme.cms.validationfield.PasswordForSave;
import com.ringme.cms.validationfield.UniqueField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldMatch(first = "password", second = "rePassword", message = "Password and repassword must match")
@UniqueField.List({
        @UniqueField(firstField = "username", firstColumn = "username", table = "tbl_user"),
        @UniqueField(firstField = "email", firstColumn = "email", table = "tbl_user")
})
@PasswordForSave
public class UserFormDto {
    private Long id;
    @NotBlank
    @Size(min = 1, max = 255)
    private String username;
    private String password;
    private String rePassword;
    @NotBlank
    @Size(min = 1, max = 200)
    private String fullname;
    @NotBlank
    @Pattern(regexp = "^(\\+|0)\\d{1,15}$", message = "Phone number must start with '+' or '0' and be followed by up to 15 digits")
    private String phone;
    @NotBlank
    @Email
    private String email;
    @NotNull
    private Boolean active;
}
