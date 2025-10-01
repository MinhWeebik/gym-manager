package com.ringme.cms.controller.gym;

import com.ringme.cms.dto.AjaxSearchDto;
import com.ringme.cms.dto.gym.MembershipDto;
import com.ringme.cms.model.gym.Member;
import com.ringme.cms.model.gym.Membership;
import com.ringme.cms.repository.gym.MembershipRepository;
import com.ringme.cms.service.gym.MembershipService;
import com.ringme.cms.utils.AppUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@Controller
@Log4j2
@RequestMapping("/membership")
@RequiredArgsConstructor
public class MembershipController {

    private final MembershipService membershipService;

    private final MembershipRepository membershipRepository;

    private final ModelMapper modelMapper;

    @RequestMapping(value = {"/index"})
    public String index(@RequestParam(name = "pageNo", required = false, defaultValue = "1") Integer pageNo,
                        @RequestParam(name = "pageSize", required = false, defaultValue = "15") Integer pageSize,
                        @RequestParam(name = "name", required = false) String name,
                        @RequestParam(name = "status", required = false) Integer status,
                        @RequestParam(name = "type", required = false) Integer type,
                        ModelMap model) {
        if(pageNo == null || pageNo <= 0) pageNo = 1;
        if(pageSize == null || pageSize <= 0) pageSize = 15;
        Page<Membership> pageObject = membershipService.getPage(name, status, type, pageNo, pageSize);
        model.put("title", "Gói thành viên");
        model.put("pageNo", pageNo);
        model.put("pageSize", pageSize);
        model.put("totalPage", pageObject.getTotalPages());
        model.put("models", pageObject.getContent());
        model.put("name", name);
        model.put("status", status);
        model.put("type", type);
        return "gym/membership/index";
    }

    @GetMapping("/ajax-search/{id}")
    @ResponseBody
    public ResponseEntity<List<AjaxSearchDto>> membershipAjaxSearch(
            @PathVariable Long id,
            @RequestParam(name = "input", required = false) String input) {
        try
        {
            return new ResponseEntity<>(membershipService.ajaxSearchMembership(input,id), HttpStatus.OK);
        }
        catch (Exception e)
        {
            log.error("Error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/form")
    public String form(@RequestParam(value = "id", required = false) Long id, ModelMap model) {
        log.info("id: {}", id);
        try {
            model.put("title", id != null ? "Cập nhật gói" : "Thêm gói");
            MembershipDto formDto = (MembershipDto) model.getOrDefault("form", new MembershipDto());
            if (id != null) {
                Membership membership = membershipRepository.findById(id).orElseThrow();
                if (!model.containsAttribute("form")) {
                    formDto = modelMapper.map(membership, MembershipDto.class);
                }
            }
            model.putIfAbsent("form", formDto);

            return "gym/membership/form";
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            return "404";
        }
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("form") MembershipDto formDto,
                       BindingResult bindingResult,
                       RedirectAttributes redirectAttributes,
                       HttpServletRequest request) {
        try {
            if (bindingResult.hasErrors()) {
                return AppUtils.goBackWithError(request, redirectAttributes, "form", bindingResult, formDto, null)
                        .orElse("redirect:/membership/form?id=" + formDto.getId());
            }
            membershipService.save(formDto);
            if (formDto.getId() == null) {
                redirectAttributes.addFlashAttribute("success", "Tạo thành công!");
                return "redirect:/membership/index";
            } else {
                redirectAttributes.addFlashAttribute("success", "Cập nhật thành công!");
            }
            return AppUtils.goBack(request).orElse("redirect:/membership/form?id=" + formDto.getId());
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            return AppUtils.goBackWithError(request, redirectAttributes, "form", bindingResult, formDto, "Error in server!")
                    .orElse("redirect:/membership/form?id=" + formDto.getId());
        }
    }

    @PostMapping(value = {"/delete/{id}"})
    public String delete(@PathVariable(required = true) Long id,
                         HttpServletRequest request,
                         RedirectAttributes redirectAttributes) {
        log.info("id: {}", id);
        try {
            membershipService.softDelete(id);
            redirectAttributes.addFlashAttribute("success", "Xóa thành công!");
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return AppUtils.goBack(request).orElse("redirect:/membership/index");
    }
}
