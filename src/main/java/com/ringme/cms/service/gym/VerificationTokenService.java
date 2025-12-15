package com.ringme.cms.service.gym;

import com.ringme.cms.dto.gym.PasswordSetupDto;
import com.ringme.cms.model.gym.Member;
import com.ringme.cms.model.gym.VerificationToken;
import com.ringme.cms.repository.gym.MemberRepository;
import com.ringme.cms.repository.gym.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class VerificationTokenService {

    private final VerificationTokenRepository verificationTokenRepository;

    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;

    public boolean isTokenValid(String token)
    {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token).orElse(null);
        return verificationToken != null;
    }

    public void updatePassword(PasswordSetupDto form)
    {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(form.getToken()).orElse(null);
        Member member = verificationToken.getMember();
        member.setPassword(passwordEncoder.encode(form.getPassword()));
        member.setStatus(1);
        memberRepository.save(member);
        verificationTokenRepository.delete(verificationToken);
    }
}
