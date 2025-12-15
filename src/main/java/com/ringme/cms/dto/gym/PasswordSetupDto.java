package com.ringme.cms.dto.gym;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class PasswordSetupDto {
    private String token;
    private String password;
    private String confirmPassword;
}
