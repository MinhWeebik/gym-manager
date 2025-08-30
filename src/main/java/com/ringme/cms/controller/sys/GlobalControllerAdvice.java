package com.ringme.cms.controller.sys;

import com.ringme.cms.dto.sys.UserProfileDto;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    @ModelAttribute
    public void addFormProfileToModel(ModelMap model) {
        model.putIfAbsent("formProfile", new UserProfileDto());
    }
}

