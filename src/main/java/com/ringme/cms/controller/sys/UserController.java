package com.ringme.cms.controller.sys;

import com.google.gson.JsonObject;
import com.ringme.cms.dto.sys.MenuFormDto;
import com.ringme.cms.dto.sys.UserFormDto;
import com.ringme.cms.dto.sys.UserProfileDto;
import com.ringme.cms.model.sys.Role;
import com.ringme.cms.model.sys.User;
import com.ringme.cms.repository.sys.RoleRepository;
import com.ringme.cms.service.sys.UserService;
import com.ringme.cms.utils.AppUtils;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@Log4j2
@RequestMapping("/sys/user")
public class UserController {
    @Autowired
    UserService userService;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    private ModelMapper modelMapper;

    @GetMapping(value = {"/index"})
    public String index(@RequestParam(name = "pageNo", required = false, defaultValue = "1") Integer pageNo,
                                 @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                                 @RequestParam(name = "username", required = false) String username,
                                 @RequestParam(name = "fullname", required = false) String fullname,
                                 @RequestParam(name = "phone", required = false) String phone,
                                 @RequestParam(name = "email", required = false) String email,
                                 ModelMap model) {
        log.info("pageNo: {}| pageSize: {}| username: {}| fullname: {}| phone: {}| email: {}",
                pageNo, pageSize, username, fullname, phone, email);
        if (pageNo == null || pageNo <= 0) pageNo = 1;
        if (pageSize == null || pageSize <= 0) pageSize = 10;

        Page<User> models = userService.getPage(pageNo, pageSize, username, fullname, phone, email);
        model.put("title", "User list");
        model.put("pageNo", pageNo);
        model.put("pageSize", pageSize);
        model.put("totalPage", models.getTotalPages());
        model.put("models", models.toList());
        model.put("username", username);
        model.put("fullname", fullname);
        model.put("phone", phone);
        model.put("email", email);
        return "sys/user";
    }

    @GetMapping("/form")
    public String form(@RequestParam(value = "id", required = false) Long id, ModelMap model) {
        log.info("id: {}", id);
        try {
            model.put("title", id != null ? "User update #" + id : "User create");

            UserFormDto formDto;
            if (id != null && !model.containsAttribute("form")) {
                formDto = modelMapper.map(userService.findByIdUser(id).orElseThrow(), UserFormDto.class);
            } else {
                formDto = new UserFormDto();
            }
            model.putIfAbsent("form", formDto);

            return "sys/user-form";
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            return "404";
        }
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("form") UserFormDto formDto,
                       BindingResult bindingResult,
                       RedirectAttributes redirectAttributes,
                       HttpServletRequest request) {
        try {
            log.info("formDto: {}", formDto);
            if (bindingResult.hasErrors()) {
                return AppUtils.goBackWithError(request, redirectAttributes, "form", bindingResult, formDto, null)
                        .orElse("redirect:/sys/user/form?id=" + formDto.getId());
            }
            userService.save(formDto);
            if (formDto.getId() == null) {
                redirectAttributes.addFlashAttribute("success", "Create success!");
                return "redirect:/sys/user/index";
            } else {
                redirectAttributes.addFlashAttribute("success", "Update success!");
            }
            return AppUtils.goBack(request).orElse("redirect:/sys/user/form?id=" + formDto.getId());
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            return AppUtils.goBackWithError(request, redirectAttributes, "form", bindingResult, formDto, "Error in server!")
                    .orElse("redirect:/sys/user/form?id=" + formDto.getId());
        }
    }

    @PostMapping(value = {"/delete/{id}"})
    public String delete(@PathVariable Long id,
                         HttpServletRequest request,
                         RedirectAttributes redirectAttributes) {
        log.info("id: {}", id);
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("success", "Delete success!");
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error in server!");
        }
        return AppUtils.goBack(request).orElse("redirect:/sys/menu/index");
    }

    @PostMapping(value = "/active-block")
    public ResponseEntity<?> activeBlock(@RequestParam("id") Long id,
                                         @RequestParam("status") boolean status) {
        log.info("id: {}| status: {}", id, status);
        JsonObject jsonObject = new JsonObject();
        try {
            jsonObject = userService.updateStatus(status, id);
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

    @GetMapping("/detail")
    public String detail(@RequestParam("id") Long id, ModelMap model) {
        log.info("id: {}", id);
        try {
            User user = userService.findByIdUser(id).orElseThrow();
            List<Role> ownedRoles = roleRepository.findAllRoleOwnedByUserId(id);
            List<Long> listId = ownedRoles.stream().map(Role::getId).collect(Collectors.toList());
            List<Role> unownedRoles = new ArrayList<>();
            if (!listId.isEmpty()) {
                unownedRoles = roleRepository.findAllRoleNotInListIdRole(listId);
            } else {
                unownedRoles = roleRepository.findAll();
            }
            model.put("title", "User detail");
            model.put("ownedRoles", ownedRoles);
            model.put("unownedRoles", unownedRoles);
            model.put("model", user);
            return "sys/user-detail";
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            return "404";
        }
    }

    @PostMapping("/profile")
    public String profile(@Valid @ModelAttribute("formProfile") UserProfileDto formDto,
                          BindingResult bindingResult,
                          RedirectAttributes redirectAttributes,
                          HttpServletRequest request) {
        try {
            log.info("formProfile: {}", formDto);
            if (bindingResult.hasErrors()) {
                redirectAttributes.addFlashAttribute("showUserProfileModal", true);
                return AppUtils.goBackWithError(request, redirectAttributes, "formProfile", bindingResult, formDto, null)
                        .orElse("redirect:/index");
            }
            userService.updateProfile(formDto);
            redirectAttributes.addFlashAttribute("success", "Update success!");
            return AppUtils.goBack(request).orElse("redirect:/index");
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("showUserProfileModal", true);
            return AppUtils.goBackWithError(request, redirectAttributes, "formProfile", bindingResult, formDto, "Error in server!")
                    .orElse("redirect:/index");
        }
    }
}
