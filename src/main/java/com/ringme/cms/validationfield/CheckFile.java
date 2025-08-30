package com.ringme.cms.validationfield;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = CheckFileValidator.class)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckFile {
    String message() default "File upload invalid.";

    String messageRequired() default "File upload is required.";

    String messageFileSize() default "The uploaded file must be smaller than %s.";

    String messageMimeTypes() default "Only %s files are allowed to be uploaded.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    long maxFileSize();

    String[] mimeTypes();

    String idField() default "id";

    String fileField();

    boolean required() default true;

    @Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface List {
        CheckFile[] value();
    }
}
