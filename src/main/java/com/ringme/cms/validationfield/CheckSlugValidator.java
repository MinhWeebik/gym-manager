package com.ringme.cms.validationfield;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class CheckSlugValidator implements ConstraintValidator<CheckSlug, String> {
    private static final String SLUG_PATTERN = "^[a-z0-9]+(?:-[a-z0-9]+)*$";
    private boolean required;

    @Override
    public void initialize(CheckSlug constraintAnnotation) {
        this.required = constraintAnnotation.required();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return !required;
        }
        return value.matches(SLUG_PATTERN);
    }
}
