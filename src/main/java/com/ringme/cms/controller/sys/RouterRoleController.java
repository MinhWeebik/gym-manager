package com.ringme.cms.controller.sys;

import com.ringme.cms.repository.sys.RoleRepository;
import com.ringme.cms.service.sys.RoleRouterService;
import com.ringme.cms.utils.AppUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Log4j2
@Controller
@RequestMapping("/sys/router-role")
public class RouterRoleController {
    @Autowired
    RoleRouterService roleRouterService;
    @Autowired
    RoleRepository roleRepository;

    @PostMapping("/createOrDelete")
    public String deleteRouterRole(@RequestParam("roleId") Long roleId,
                                   @RequestParam(name = "routerIds", required = false) List<Long> routerIds,
                                   @RequestParam("type") Boolean type,
                                   RedirectAttributes redirectAttributes,
                                   HttpServletRequest request) {
        log.info("id: {}| listRouter: {}", roleId, routerIds);

        try {
            if (routerIds == null || routerIds.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Please select a router!");
            } else {
                roleRepository.findById(roleId).orElseThrow();
                if (type) {
                    roleRouterService.createRoleRouter(roleId, routerIds);
                } else {
                    roleRouterService.deleteRoleRouter(roleId, routerIds);
                }
                redirectAttributes.addFlashAttribute("success", "Success!");
            }
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error in server!");
        }

        return AppUtils.goBack(request).orElse("redirect:/sys/role/detail?id=" + roleId);
    }
}
