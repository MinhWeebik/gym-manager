package com.ringme.cms.validationfield;

import com.ringme.cms.dto.sys.UserSecurity;
import com.ringme.cms.service.sys.UserService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@Component
@Log4j2
public class VerifyPasswordValidator implements ConstraintValidator<VerifyPassword, String> {
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    UserService userService;

    @Override
    public void initialize(VerifyPassword constraintAnnotation) {
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        boolean valid = false;

        try {
            UserSecurity userSecurity = (UserSecurity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String passwordHash = userService.findByIdUser(userSecurity.getId()).orElseThrow().getPassword();
            valid = passwordEncoder.matches(password, passwordHash);
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
        }

        log.info("valid: {}", valid);
        return valid;
    }
}