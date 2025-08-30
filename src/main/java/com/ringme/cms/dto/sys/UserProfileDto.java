package com.ringme.cms.dto.sys;

import com.ringme.cms.validationfield.FieldMatch;
import com.ringme.cms.validationfield.PasswordForSave;
import com.ringme.cms.validationfield.VerifyPassword;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldMatch(first = "password", second = "rePassword", message = "Password and repassword must match")
@PasswordForSave
public class UserProfileDto {
    @VerifyPassword
    private String passwordOld;
    private String password;
    private String rePassword;
}
