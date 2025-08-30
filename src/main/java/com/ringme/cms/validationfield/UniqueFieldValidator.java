package com.ringme.cms.validationfield;

import com.ringme.cms.dao.ValidateDao;
import com.ringme.cms.utils.AppUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@Component
@Log4j2
public class UniqueFieldValidator implements ConstraintValidator<UniqueField, Object> {

    @Autowired
    private ValidateDao validateDao;

    private String table;
    private String firstField;
    private String firstColumn;
    private String secondField;
    private String secondColumn;
    private String message;

    @Override
    public void initialize(UniqueField constraintAnnotation) {
        firstField = constraintAnnotation.firstField();
        firstColumn = constraintAnnotation.firstColumn();
        secondField = constraintAnnotation.secondField();
        secondColumn = constraintAnnotation.secondColumn();
        table = constraintAnnotation.table();
        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        boolean valid;

        try {
            Object firstValue = AppUtils.getFieldValue(value, firstField).orElse(null);
            Object secondValue = AppUtils.getFieldValue(value, secondField).orElse(null);
            valid = !validateDao.isExist(firstValue, firstColumn, secondValue, secondColumn, table);
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            valid = false;
        }

        log.info("valid: {}", valid);
        if (!valid) {
            context.buildConstraintViolationWithTemplate(message)
                    .addPropertyNode(firstField)
                    .addConstraintViolation()
                    .disableDefaultConstraintViolation();
        }

        return valid;
    }
}