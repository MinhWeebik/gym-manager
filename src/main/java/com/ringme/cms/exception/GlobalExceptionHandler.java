package com.ringme.cms.exception;

import javassist.NotFoundException;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

@ControllerAdvice
@Log4j2
public class GlobalExceptionHandler {
    @ExceptionHandler(value = {IllegalArgumentException.class})
    protected String illegalArgumentExceptionHandler(IllegalArgumentException ex, Model model) {
        log.error(ex.getMessage());
        model.addAttribute("errorMessage", ex.getMessage());
        return "404";
    }

    @ExceptionHandler(value = {NotFoundException.class})
    protected String illegalArgumentExceptionHandler(NotFoundException ex, Model model) {
        log.error(ex.getMessage());
        model.addAttribute("errorMessage", ex.getMessage());
        return "404";
    }
}
