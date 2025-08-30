package com.ringme.cms.validationfield;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class OneOfStringsValidator implements ConstraintValidator<OneOfStrings, String> {

    private boolean forcedIgnoreCase;

    private String[] values = new String[]{};

    @Override
    public void initialize(OneOfStrings constraintAnnotation) {
        this.forcedIgnoreCase = constraintAnnotation.forcedIgnoreCase();
        this.values = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintContext) {
        if (values == null) return true;
        if (value == null) return false;

        for (String item : values) {
            if (forcedIgnoreCase) {
                if (item.equalsIgnoreCase(value)) return true;
            } else {
                if (item.equals(value)) return true;
            }
        }
        return false;
    }
}
