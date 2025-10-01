package com.ringme.cms.controller.gym;

import com.ringme.cms.dto.AjaxSearchDto;
import com.ringme.cms.dto.ValueTextDto;
import com.ringme.cms.dto.gym.MemberDto;
import com.ringme.cms.dto.gym.TrainerDto;
import com.ringme.cms.model.gym.Trainer;
import com.ringme.cms.repository.gym.TrainerRepository;
import com.ringme.cms.service.gym.TrainerService;
import com.ringme.cms.utils.AppUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@Log4j2
@RequestMapping("/trainer")
@RequiredArgsConstructor
public class TrainerController {

    private final TrainerService trainerService;

    private final TrainerRepository trainerRepository;

    private final ModelMapper modelMapper;

    @RequestMapping(value = {"/index"})
    public String index(@RequestParam(name = "pageNo", required = false, defaultValue = "1") Integer pageNo,
                        @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                        @RequestParam(name = "name", required = false) String name,
                        @RequestParam(name = "status", required = false) Integer status,
                        @RequestParam(name = "gender", required = false) Integer gender,
                        @RequestParam(name = "email", required = false) String email,
                        @RequestParam(name = "phoneNumber", required = false) String phoneNumber,
                        ModelMap model) {
        if(pageNo == null || pageNo <= 0) pageNo = 1;
        if(pageSize == null || pageSize <= 0) pageSize = 10;

        Page<Trainer> pageObject = trainerService.getPage(name, status, gender, email, phoneNumber, pageNo, pageSize);
        model.put("title", "Huấn luyện viên");
        model.put("pageNo", pageNo);
        model.put("pageSize", pageSize);
        model.put("totalPage", pageObject.getTotalPages());
        model.put("models", pageObject.toList());
        model.put("name", name);
        model.put("status", status);
        model.put("gender", gender);
        model.put("email", email);
        model.put("phoneNumber", phoneNumber);
        return "gym/trainer/index";
    }

    @GetMapping("/form")
    @Transactional(readOnly = true)
    public String form(@RequestParam(value = "id", required = false) Long id, ModelMap model) {
        log.info("id: {}", id);
        try {
            model.put("title", id != null ? "Sửa huấn luyện viên" : "Thêm huấn luyện viên");
            TrainerDto formDto = (TrainerDto) model.getOrDefault("model", new TrainerDto());
            if (id != null) {
                Trainer trainer = trainerRepository.findById(id).orElseThrow();
                if (!model.containsAttribute("model")) {
                    formDto = modelMapper.map(trainer, TrainerDto.class);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                    formDto.setDateOfBirthString(trainer.getDateOfBirth().format(formatter));
                    formDto.setHireDateString(trainer.getHireDate().format(dateTimeFormatter));
                }
                model.put("image", trainer.getImageUrl());
            }
            else {
                formDto.setGender(1);
            }
            model.putIfAbsent("model", formDto);

            return "gym/trainer/form";
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            return "404";
        }
    }

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

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("model") TrainerDto formDto,
                       BindingResult bindingResult,
                       RedirectAttributes redirectAttributes,
                       HttpServletRequest request) {
        try {
            if (bindingResult.hasErrors()) {
                return AppUtils.goBackWithError(request, redirectAttributes, "model", bindingResult, formDto, null)
                        .orElse("redirect:/trainer/form?id=" + formDto.getId());
            }
            trainerService.save(formDto);
            if (formDto.getId() == null) {
                redirectAttributes.addFlashAttribute("success", "Tạo thành công!");
                return "redirect:/trainer/index";
            } else {
                redirectAttributes.addFlashAttribute("success", "Cập nhật thành công!");
            }
            return AppUtils.goBack(request).orElse("redirect:/trainer/form?id=" + formDto.getId());
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            return AppUtils.goBackWithError(request, redirectAttributes, "model", bindingResult, formDto, "Error in server!")
                    .orElse("redirect:/trainer/form?id=" + formDto.getId());
        }
    }

    @PostMapping(value = {"/delete/{id}"})
    public String delete(@PathVariable(required = true) Long id,
                         HttpServletRequest request,
                         RedirectAttributes redirectAttributes) {
        log.info("id: {}", id);
        try {
            trainerService.softDelete(id);
            redirectAttributes.addFlashAttribute("success", "Xóa thành công!");
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return AppUtils.goBack(request).orElse("redirect:/trainer/index");
    }
}
