package com.ringme.cms.exception;

public class CustomExceptionWithText extends RuntimeException {
    public CustomExceptionWithText(String message) {
        super(message);
    }
}
