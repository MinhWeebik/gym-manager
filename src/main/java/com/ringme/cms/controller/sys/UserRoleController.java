package com.ringme.cms.controller.sys;

import com.ringme.cms.repository.sys.RoleRepository;
import com.ringme.cms.repository.sys.UserRepository;
import com.ringme.cms.service.sys.RoleRouterService;
import com.ringme.cms.service.sys.UserRoleService;
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
@RequestMapping("/sys/user-role")
public class UserRoleController {
    @Autowired
    UserRepository userRepository;
    @Autowired
    UserRoleService userRoleService;

    @PostMapping("/createOrDelete")
    public String deleteRouterRole(@RequestParam("userId") Long userId,
                                   @RequestParam(name = "roleIds", required = false) List<Long> roleIds,
                                   @RequestParam("type") Boolean type,
                                   RedirectAttributes redirectAttributes,
                                   HttpServletRequest request) {
        log.info("userId: {}| roleIds: {}", userId, roleIds);

        try {
            if (roleIds == null || roleIds.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Please select a role!");
            } else {
                userRepository.findById(userId).orElseThrow();
                if (type) {
                    userRoleService.createUserRole(userId, roleIds);
                } else {
                    userRoleService.deleteUserRole(userId, roleIds);
                }
                redirectAttributes.addFlashAttribute("success", "Success!");
            }
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error in server!");
        }

        return AppUtils.goBack(request).orElse("redirect:/sys/user/detail?id=" + userId);
    }
}
