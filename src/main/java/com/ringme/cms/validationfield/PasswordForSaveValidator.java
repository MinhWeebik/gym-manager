package com.ringme.cms.validationfield;

import com.ringme.cms.utils.AppUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@Component
@Log4j2
public class PasswordForSaveValidator implements ConstraintValidator<PasswordForSave, Object> {
    private static final String PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\W).{8,}$";

    private String passwordField;
    private String idField;
    private String message;

    @Override
    public void initialize(PasswordForSave constraintAnnotation) {
        passwordField = constraintAnnotation.passwordField();
        idField = constraintAnnotation.idField();
        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        boolean valid;

        try {
            String passwordValue = (String) AppUtils.getFieldValue(value, passwordField).orElse(null);
            Object idValue = AppUtils.getFieldValue(value, idField).orElse(null);

            if ((passwordValue == null || passwordValue.isEmpty()) &&
                    idValue != null) {
                valid = true;
            } else {
                if (passwordValue == null) {
                    valid = false;
                } else {
                    valid = passwordValue.matches(PASSWORD_PATTERN);
                }
            }
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            valid = false;
        }

        log.info("valid: {}", valid);
        if (!valid) {
            context.buildConstraintViolationWithTemplate(message)
                    .addPropertyNode(passwordField)
                    .addConstraintViolation()
                    .disableDefaultConstraintViolation();
        }

        return valid;
    }
}