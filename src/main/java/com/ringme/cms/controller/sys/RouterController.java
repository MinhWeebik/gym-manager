package com.ringme.cms.controller.sys;


import com.google.gson.JsonObject;
import com.ringme.cms.dto.sys.RouterFormDto;
import com.ringme.cms.model.sys.Router;
import com.ringme.cms.service.sys.RouterService;
import com.ringme.cms.utils.AppUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Controller
@Log4j2
@RequestMapping("/sys/router")
public class RouterController {
    @Autowired
    RouterService routerService;
    @Autowired
    private MessageSource messageSource;

    @GetMapping(value = {"/index"})
    public String index(@RequestParam(name = "pageNo", required = false, defaultValue = "1") Integer pageNo,
                             @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                             ModelMap model) {
        log.info("pageNo: {}| pageSize: {}", pageNo, pageSize);
        if (pageNo == null || pageNo <= 0) pageNo = 1;
        if (pageSize == null || pageSize <= 0) pageSize = 10;

        Page<Router> models = routerService.page(pageNo, pageSize);
        model.put("title", "Router");
        model.put("pageNo", pageNo);
        model.put("pageSize", pageSize);
        model.put("totalPage", models.getTotalPages());
        model.put("models", models.toList());
        if (!model.containsAttribute("form")) {
            model.put("form", new RouterFormDto());
        }
        return "sys/router";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("form") RouterFormDto formDto,
                       BindingResult bindingResult,
                       RedirectAttributes redirectAttributes,
                       HttpServletRequest request) {
        try {
            log.info("RouterDto: {}", formDto);
            if (bindingResult.hasErrors()) {
                return AppUtils.goBackWithError(request, redirectAttributes, "form", bindingResult, formDto, null)
                        .orElse("redirect:/sys/router/index");
            }
            routerService.save(formDto);
            if (formDto.getId() == null) {
                redirectAttributes.addFlashAttribute("success", "Create success!");
            } else {
                redirectAttributes.addFlashAttribute("success", "Update success!");
            }
            return AppUtils.goBack(request).orElse("redirect:/sys/router/index");
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            return AppUtils.goBackWithError(request, redirectAttributes, "form", bindingResult, formDto, "Error in server!")
                    .orElse("redirect:/sys/router/index");
        }
    }

    @PostMapping(value = "/active-block")
    public ResponseEntity<?> activeBlock(@RequestParam("id") Long id, @RequestParam("status") boolean status) {
        log.info("id: {}| status: {}", id, status);
        JsonObject jsonObject = new JsonObject();
        try {
            jsonObject = routerService.updateStatus(status, id);
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            jsonObject.addProperty("code", 500);
            jsonObject.addProperty("status", false);
            jsonObject.addProperty("msg", "Error in server!");
        }
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(jsonObject.toString());
    }

    @PostMapping(value = {"/delete/{id}"})
    public String delete(@PathVariable(required = true) Long id,
                         HttpServletRequest request,
                         RedirectAttributes redirectAttributes) {
        log.info("id: {}", id);
        try {
            routerService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Delete success!");
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error in server!");
        }
        return AppUtils.goBack(request).orElse("redirect:/sys/router/index");
    }
}
