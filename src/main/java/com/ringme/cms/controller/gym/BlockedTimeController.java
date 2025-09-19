package com.ringme.cms.controller.gym;

import com.ringme.cms.dto.gym.BlockedTimeDto;
import com.ringme.cms.dto.gym.ScheduleDto;
import com.ringme.cms.model.gym.BlockedTime;
import com.ringme.cms.model.gym.ScheduledClass;
import com.ringme.cms.repository.gym.BlockedTimeRepository;
import com.ringme.cms.repository.gym.TrainerRepository;
import com.ringme.cms.service.gym.BlockedTimeService;
import com.ringme.cms.utils.AppUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.format.DateTimeFormatter;

@Controller
@Log4j2
@RequestMapping("/blocked-time")
@RequiredArgsConstructor
public class BlockedTimeController {

    private final BlockedTimeRepository blockedTimeRepository;

    private final ModelMapper modelMapper;

    private final TrainerRepository trainerRepository;
    private final BlockedTimeService blockedTimeService;

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("form") BlockedTimeDto formDto,
                       BindingResult bindingResult,
                       RedirectAttributes redirectAttributes,
                       HttpServletRequest request) {
        try {
            if (bindingResult.hasErrors()) {
                return AppUtils.goBackWithError(request, redirectAttributes, "form", bindingResult, formDto, null)
                        .orElse("redirect:/schedule/form-blocked-time?view=" + formDto.getView() + "&date="
                                + formDto.getDate() + "&startDate=" + formDto.getStartDateStr() + "&startTime="
                                + formDto.getFromStr() + "&endTime=" + formDto.getToStr());
            }
            blockedTimeService.save(formDto);
            if (formDto.getId() == null) {
                redirectAttributes.addFlashAttribute("success", "Tạo thành công!");
                return "redirect:/schedule/index?view=" + formDto.getView() + "&date=" + formDto.getDate();
            } else {
                redirectAttributes.addFlashAttribute("success", "Cập nhật thành công!");
            }
            return "redirect:/schedule/index?view=" + formDto.getView() + "&date=" + formDto.getDate();
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            return AppUtils.goBackWithError(request, redirectAttributes, "form", bindingResult, formDto, "Error in server!")
                    .orElse("redirect:/schedule/form-blocked-time?view=" + formDto.getView() + "&date="
                            + formDto.getDate() + "&startDate=" + formDto.getStartDateStr() + "&startTime="
                            + formDto.getFromStr() + "&endTime=" + formDto.getToStr());
        }
    }
}
