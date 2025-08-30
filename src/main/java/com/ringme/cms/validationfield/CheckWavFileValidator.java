package com.ringme.cms.validationfield;

import com.ringme.cms.utils.AppUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.io.ByteArrayInputStream;
import java.util.Arrays;

public class CheckWavFileValidator implements ConstraintValidator<CheckWavFile, Object> {
    private long maxFileSize;
    private String[] mimeTypes;
    private long durationFrom;
    private long durationTo;
    private long bitRate;
    private long channels;
    private long audioSampleSize;
    private String audioFormat;
    private String idField;
    private String fileField;
    private boolean required;
    private String message;
    private String messageRequired;
    private String messageFileSize;
    private String messageMimeTypes;
    private String messageFileDuration;
    private String messageBitRate;
    private String messageChannels;
    private String messageAudioSampleSize;
    private String messageAudioFormat;

    @Override
    public void initialize(CheckWavFile constraintAnnotation) {
        this.maxFileSize = constraintAnnotation.maxFileSize();
        this.mimeTypes = constraintAnnotation.mimeTypes();
        this.durationFrom = constraintAnnotation.durationFrom();
        this.durationTo = constraintAnnotation.durationTo();
        this.bitRate = constraintAnnotation.bitRate();
        this.channels = constraintAnnotation.channels();
        this.audioSampleSize = constraintAnnotation.audioSampleSize();
        this.audioFormat = constraintAnnotation.audioFormat();
        this.idField = constraintAnnotation.idField();
        this.fileField = constraintAnnotation.fileField();
        this.required = constraintAnnotation.required();
        this.message = constraintAnnotation.message();
        this.messageRequired = constraintAnnotation.messageRequired();
        this.messageFileSize = constraintAnnotation.messageFileSize();
        this.messageMimeTypes = constraintAnnotation.messageMimeTypes();
        this.messageFileDuration = constraintAnnotation.messageFileDuration();
        this.messageBitRate = constraintAnnotation.messageBitRate();
        this.messageChannels = constraintAnnotation.messageChannels();
        this.messageAudioSampleSize = constraintAnnotation.messageAudioSampleSize();
        this.messageAudioFormat = constraintAnnotation.messageAudioFormat();
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
                return validateFile(file, context);
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
    private boolean validateFile(MultipartFile file, ConstraintValidatorContext context) {
        if (file.getSize() > this.maxFileSize) {
            return addViolation(context, messageFileSize);
        }

        if (this.mimeTypes.length > 0 && !Arrays.asList(this.mimeTypes).contains(file.getContentType())) {
            return addViolation(context, messageMimeTypes);
        }

        try (AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new ByteArrayInputStream(file.getBytes()))) {
            AudioFormat format = audioInputStream.getFormat();

            float bitRate = format.getSampleRate() * format.getSampleSizeInBits() * format.getChannels() / 1000; // kpbs
            if (bitRate != this.bitRate) {
                return addViolation(context, messageBitRate);
            }

            if (format.getChannels() != this.channels) {
                return addViolation(context, messageChannels);
            }

            if (format.getSampleSizeInBits() != this.audioSampleSize) {
                return addViolation(context, messageAudioSampleSize);
            }

            if (!format.getEncoding().toString().equals(this.audioFormat)) {
                return addViolation(context, messageAudioFormat);
            }

            long durationInMillis = (audioInputStream.getFrameLength() * 1000) / (int) format.getFrameRate();
            if (durationInMillis < this.durationFrom || durationInMillis > this.durationTo) {
                return addViolation(context, messageFileDuration);
            }
        } catch (Exception e) {
            return addViolation(context, message);
        }

        return true;
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
