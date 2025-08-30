package com.ringme.cms.validationfield;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = CheckWavFileValidator.class)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckWavFile {
    String message() default "File upload invalid.";

    String messageRequired() default "File upload is required.";

    String messageFileSize() default "The uploaded file must be smaller than 500 KB.";

    String messageFileDuration() default "File upload must take about 40s to 60s.";

    String messageMimeTypes() default "Only wav files are allowed to be uploaded.";

    String messageBitRate() default "Bit rate must be 64 kbs.";

    String messageChannels() default "Channels must be 1.";

    String messageAudioSampleSize() default "Audio sample size must be 8 bit.";

    String messageAudioFormat() default "Audio format must be CCITT A-Law.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    boolean required() default true;

    long maxFileSize() default 500 * 1024;

    String[] mimeTypes() default {"audio/vnd.wave", "audio/wav"};

    long durationFrom() default 40 * 1000;

    long durationTo() default 60 * 1000;

    long bitRate() default 64;

    long channels() default 1;

    long audioSampleSize() default 8;

    String audioFormat() default "ALAW";

    String idField() default "id";

    String fileField();

    @Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface List {
        CheckWavFile[] value();
    }
}
