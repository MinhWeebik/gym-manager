package com.ringme.cms.controller.sys;

import com.ringme.cms.dto.sys.RoleFormDto;
import com.ringme.cms.model.sys.Role;
import com.ringme.cms.model.sys.Router;
import com.ringme.cms.model.sys.RouterRole;
import com.ringme.cms.service.sys.RoleRouterService;
import com.ringme.cms.service.sys.RoleService;
import com.ringme.cms.service.sys.RouterService;
import com.ringme.cms.utils.AppUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@Log4j2
@RequestMapping("/sys/role")
public class RoleController {
    @Autowired
    RoleService roleService;
    @Autowired
    private RoleRouterService roleRouterService;
    @Autowired
    private RouterService routerService;

    @GetMapping(value = {"/index"})
    public String index(@RequestParam(name = "pageNo", required = false, defaultValue = "1") Integer pageNo,
                             @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                             @RequestParam(name = "search", required = false) String search,
                             ModelMap model) {
        log.info("pageNo: {}| pageSize: {}| search: {}", pageNo, pageSize, search);
        if (pageNo == null || pageNo <= 0) pageNo = 1;
        if (pageSize == null || pageSize <= 0) pageSize = 10;

        Page<Role> models = roleService.page(pageNo, pageSize, search);
        model.put("title", "Role");
        model.put("pageNo", pageNo);
        model.put("pageSize", pageSize);
        model.put("totalPage", models.getTotalPages());
        model.put("models", models.toList());
        if (!model.containsAttribute("form")) {
            model.put("form", new RoleFormDto());
        }
        model.put("search", search);
        return "sys/role";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("form") RoleFormDto formDto,
                       BindingResult bindingResult,
                       RedirectAttributes redirectAttributes,
                       HttpServletRequest request) {
        try {
            log.info("formDto: {}", formDto);
            if (bindingResult.hasErrors()) {
                return AppUtils.goBackWithError(request, redirectAttributes, "form", bindingResult, formDto, null)
                        .orElse("redirect:/sys/role/index");
            }
            roleService.save(formDto);
            if (formDto.getId() == null) {
                redirectAttributes.addFlashAttribute("success", "Create success!");
            } else {
                redirectAttributes.addFlashAttribute("success", "Update success!");
            }
            return AppUtils.goBack(request).orElse("redirect:/sys/role/index");
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            return AppUtils.goBackWithError(request, redirectAttributes, "form", bindingResult, formDto, "Error in server!")
                    .orElse("redirect:/sys/role/index");
        }
    }

    @PostMapping(value = {"/delete/{id}"})
    public String delete(@PathVariable Long id,
                         HttpServletRequest request,
                         RedirectAttributes redirectAttributes) {
        log.info("id: {}", id);
        try {
            roleService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Delete success!");
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error in server!");
        }
        return AppUtils.goBack(request).orElse("redirect:/sys/role/index");
    }

    @GetMapping("/detail")
    public String detail(@RequestParam("id") Long id, ModelMap model) {
        log.info("id: {}", id);
        try {
            Role role = roleService.findRoleById(id).orElseThrow();
            List<RouterRole> ownedRoutes = roleRouterService.findAllRouterRoleByRoleId(id);
            List<Long> listId = ownedRoutes.stream().map(e -> e.getRouter().getId()).collect(Collectors.toList());
            List<Router> unownedRoutes;
            if (!listId.isEmpty()) {
                unownedRoutes = routerService.findAllRouterNotInRole(listId);
            } else {
                unownedRoutes = routerService.findAllRouterActive();
            }
            model.put("title", "Role detail");
            model.put("ownedRoutes", ownedRoutes);
            model.put("unownedRoutes", unownedRoutes);
            model.put("model", role);
            return "sys/role-detail";
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            return "404";
        }
    }
}
