package com.ringme.cms.validationfield;

import com.ringme.cms.utils.AppUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class FieldMatchValidator implements ConstraintValidator<FieldMatch, Object> {
    private String firstFieldName;
    private String secondFieldName;
    private String message;

    @Override
    public void initialize(FieldMatch constraintAnnotation) {
        firstFieldName = constraintAnnotation.first();
        secondFieldName = constraintAnnotation.second();
        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        boolean valid = true;

        try {
            Object firstObj = AppUtils.getFieldValue(value, firstFieldName).orElse(null);
            Object secondObj = AppUtils.getFieldValue(value, secondFieldName).orElse(null);
            valid = firstObj == null && secondObj == null || firstObj != null && firstObj.equals(secondObj);
        } catch (Exception ignore) {
            valid = false;
        }

        if (!valid) {
            context.buildConstraintViolationWithTemplate(message)
                    .addPropertyNode(firstFieldName)
                    .addConstraintViolation()
                    .disableDefaultConstraintViolation();
        }

        return valid;
    }
}
