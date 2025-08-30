package com.ringme.cms.validationfield;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.ringme.cms.utils.AppUtils;
import org.springframework.web.multipart.MultipartFile;
import java.util.Base64;
import java.util.Arrays;

public class CheckFileValidator implements ConstraintValidator<CheckFile, Object> {

    private long maxFileSize;
    private String[] mimeTypes;
    private String idField;
    private String fileField;
    private boolean required;
    private String messageRequired;
    private String messageFileSize;
    private String messageMimeTypes;

    @Override
    public void initialize(CheckFile constraintAnnotation) {
        this.maxFileSize = constraintAnnotation.maxFileSize();
        this.mimeTypes = constraintAnnotation.mimeTypes();
        this.idField = constraintAnnotation.idField();
        this.fileField = constraintAnnotation.fileField();
        this.required = constraintAnnotation.required();
        this.messageRequired = constraintAnnotation.messageRequired();
        this.messageFileSize = constraintAnnotation.messageFileSize();
        this.messageMimeTypes = constraintAnnotation.messageMimeTypes();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        Object idValue = AppUtils.getFieldValue(value, idField).orElse(null);
        Object fileValue = AppUtils.getFieldValue(value, fileField).orElse(null);

        if (isFileRequiredButEmpty(idValue, fileValue)) {
            return addViolation(context, messageRequired);
        }

        if (fileValue instanceof MultipartFile file) {
            if (!file.isEmpty()) {
                return validateFile(file.getSize(), file.getContentType(), context);
            }
        } else if (fileValue instanceof String base64File) {
            if (!base64File.isEmpty()) {
                return validateBase64File(base64File, context);
            }
        } else {
            return addViolation(context, messageRequired);
        }

        return true;
    }

    // Kiểm tra xem file có bắt buộc nhưng rỗng không
    private boolean isFileRequiredButEmpty(Object idValue, Object fileValue) {
        return idValue == null && required && (fileValue == null || fileValue.toString().isEmpty());
    }

    // Kiểm tra file kích thước và MIME type
    private boolean validateFile(long fileSize, String contentType, ConstraintValidatorContext context) {
        if (fileSize > maxFileSize) {
            return addViolation(context, messageFileSize);
        }

        if (mimeTypes.length > 0 && !Arrays.asList(mimeTypes).contains(contentType)) {
            return addViolation(context, messageMimeTypes);
        }

        return true;
    }

    // Kiểm tra file Base64
    private boolean validateBase64File(String base64, ConstraintValidatorContext context) {
        try {
            String[] base64Parts = base64.split(",");
            byte[] decodedBytes = Base64.getDecoder().decode(base64Parts[1]);

            String base64MimeType = base64Parts[0].split(":")[1].split(";")[0];
            return validateFile(decodedBytes.length, base64MimeType, context);
        } catch (IllegalArgumentException e) {
            return addViolation(context, "Invalid base64 format");
        }
    }

    // Thêm thông báo lỗi vào context
    private boolean addViolation(ConstraintValidatorContext context, String message) {
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(fileField)
                .addConstraintViolation()
                .disableDefaultConstraintViolation();
        return false;
    }
}
