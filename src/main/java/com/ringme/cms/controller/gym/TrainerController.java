package com.ringme.cms.controller.gym;

import com.ringme.cms.dto.AjaxSearchDto;
import com.ringme.cms.service.gym.TrainerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@Log4j2
@RequestMapping("/trainer")
@RequiredArgsConstructor
public class TrainerController {

    private final TrainerService trainerService;

    @GetMapping("/ajax-search")
    @ResponseBody
    public ResponseEntity<List<AjaxSearchDto>> trainerAjaxSearch(
            @RequestParam(name = "input", required = false) String input) {
        try
        {
            return new ResponseEntity<>(trainerService.ajaxSearchTrainer(input), HttpStatus.OK);
        }
        catch (Exception e)
        {
            log.error("Error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
