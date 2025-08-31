package com.ringme.cms.service.gym;

import com.ringme.cms.common.Helper;
import com.ringme.cms.dto.AjaxSearchDto;
import com.ringme.cms.repository.gym.MembershipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class MembershipService {

    private final MembershipRepository membershipRepository;

    public List<AjaxSearchDto> ajaxSearchMembership(String input) {
        return Helper.listAjax(membershipRepository.ajaxSearchMembership(Helper.processStringSearch(input)));
    }
}
