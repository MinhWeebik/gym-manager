package com.ringme.cms.controller.gym;

import com.ringme.cms.dto.AjaxSearchDto;
import com.ringme.cms.service.gym.ClassService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@Log4j2
@RequestMapping("/class")
@RequiredArgsConstructor
public class ClassController {

    private final ClassService classService;

    @GetMapping("/ajax-search")
    @ResponseBody
    public ResponseEntity<List<AjaxSearchDto>> classAjaxSearch(
            @RequestParam(name = "input", required = false) String input) {
        try
        {
            return new ResponseEntity<>(classService.ajaxSearchClass(input), HttpStatus.OK);
        }
        catch (Exception e)
        {
            log.error("Error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
