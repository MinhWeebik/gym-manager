package com.ringme.cms.controller.gym;

import com.ringme.cms.dto.gym.PasswordSetupDto;
import com.ringme.cms.model.gym.Member;
import com.ringme.cms.model.gym.VerificationToken;
import com.ringme.cms.repository.gym.VerificationTokenRepository;
import com.ringme.cms.service.gym.VerificationTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@Log4j2
public class OnboardingController {

    private final VerificationTokenService verificationTokenService;
    private final VerificationTokenRepository verificationTokenRepository;

    @GetMapping("/setup-password")
    public String showSetupPage(@RequestParam("token") String token, ModelMap model) {
        if (!verificationTokenService.isTokenValid(token)) {
            return "gym/member/error-page";
        }
        PasswordSetupDto dto = new PasswordSetupDto();
        dto.setToken(token);
        boolean isReset = false;
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token).orElseThrow();
        Member member = verificationToken.getMember();
        if(member.getPassword() != null)
        {
            isReset = true;
        }
        model.put("isReset", isReset);
        model.put("setupForm", dto);
        return "gym/member/setup-password";
    }

    @PostMapping("/setup-password")
    public String processSetup(@ModelAttribute("setupForm") PasswordSetupDto form, ModelMap model) {

        if (!verificationTokenService.isTokenValid(form.getToken())) {
            model.addAttribute("error", "Token không hợp lệ");
            return "gym/member/setup-password";
        }

        if (!form.getPassword().equals(form.getConfirmPassword())) {
            model.addAttribute("error", "Mật khẩu không giống nhau");
            return "gym/member/setup-password";
        }

        verificationTokenService.updatePassword(form);

        return "gym/member/success-page";
    }
}
