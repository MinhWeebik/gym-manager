package com.ringme.cms.controller.gym;

import com.ringme.cms.dto.gym.MemberDto;
import com.ringme.cms.dto.gym.MemberSubscriptionDto;
import com.ringme.cms.model.gym.MemberSubscription;
import com.ringme.cms.repository.gym.MemberSubscriptionRepository;
import com.ringme.cms.service.music.MemberSubscriptionService;
import com.ringme.cms.utils.AppUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;

@Controller
@Log4j2
@RequestMapping("/subscription")
@RequiredArgsConstructor
public class MemberSubscriptionController {

    private final MemberSubscriptionRepository memberSubscriptionRepository;

    private final ModelMapper modelMapper;

    private final MemberSubscriptionService memberSubscriptionService;

    @GetMapping("/update")
    public String update(@RequestParam(value = "id") Long id, ModelMap model) {
        log.info("id: {}", id);
        try {
            MemberSubscriptionDto formDto = (MemberSubscriptionDto) model.getOrDefault("model", new MemberSubscriptionDto());
            MemberSubscription memberSubscription = memberSubscriptionRepository.findById(id).orElseThrow();
            if (!model.containsAttribute("model")) {
                formDto = modelMapper.map(memberSubscription, MemberSubscriptionDto.class);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                formDto.setStartEndString(memberSubscription.getStartAt().format(formatter) + " - "  + memberSubscription.getEndAt().format(formatter));
            }
            model.putIfAbsent("model", formDto);

            return "gym/fragment/member :: editMemberSubscription";
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            return "404";
        }
    }

    @GetMapping("/detail")
    public String detail(@RequestParam(value = "id") Long id, ModelMap model) {
        log.info("id: {}", id);
        try {
            MemberSubscription memberSubscription = memberSubscriptionRepository.findById(id).orElseThrow();
            model.putIfAbsent("model", memberSubscription);

            return "gym/fragment/member :: memberSubscriptionDetail";
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            return "404";
        }
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("model") MemberSubscriptionDto formDto,
                       BindingResult bindingResult,
                       RedirectAttributes redirectAttributes,
                       HttpServletRequest request) {
        try {
            if (bindingResult.hasErrors()) {
                return AppUtils.goBackWithError(request, redirectAttributes, "form", bindingResult, formDto, null)
                        .orElse("redirect:/member/detail?id=" + formDto.getId() + "&tab=2");
            }
            memberSubscriptionService.save(formDto);
            if (formDto.getId() == null) {
                redirectAttributes.addFlashAttribute("success", "Tạo thành công!");
                return "redirect:/member/detail?id=" + formDto.getId() + "&tab=2";
            } else {
                redirectAttributes.addFlashAttribute("success", "Cập nhật thành công!");
            }
            return AppUtils.goBack(request).orElse("redirect:/member/detail?id=" + formDto.getId() + "&tab=2");
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            return AppUtils.goBackWithError(request, redirectAttributes, "form", bindingResult, formDto, "Error in server!")
                    .orElse("redirect:/member/detail?id=" + formDto.getId() + "&tab=2");
        }
    }
}
