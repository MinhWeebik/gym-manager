package com.ringme.cms.controller.sys;


import com.ringme.cms.dto.sys.UserSecurity;
import com.ringme.cms.service.sys.UserService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;

@Controller
@Log4j2
public class LoginController {
    @Autowired
    BCryptPasswordEncoder passwordEncoder;
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String login(Model model, HttpServletRequest request, String username, String password) {
        if (SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof UserSecurity) {
            return "redirect:/index";
        }
        return "login";
    }

    @GetMapping("/login-error")
    public String loginerror(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", "The username or password is incorrect. Please try again!");
        return "redirect:/login";
    }

    @GetMapping("/login/captcha-fail")
    public String captchaFail(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", "The CAPTCHA is incorrect. Please try again!");
        return "redirect:/login";
    }

    @GetMapping("/")
    public String getHome() {
        return "redirect:/index";
    }

    @PostMapping("/login")
    public String login(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", "Captcha không chính xác!");
        return "redirect:/login";
    }

    @GetMapping({"/403"})
    public String error403() {
        log.info(passwordEncoder.encode("ERROR 403"));
        return "403";
    }

    @GetMapping({"/404"})
    public String error404() {
        log.info(passwordEncoder.encode("123456"));
        return "404";
    }

    @GetMapping("/index")
    public String index(HttpServletRequest httpServletRequest, Model model) {
        return "index";
    }
}