package com.ringme.cms.service.gym;

import com.ringme.cms.common.Helper;
import com.ringme.cms.dto.AjaxSearchDto;
import com.ringme.cms.model.gym.MemberSubscription;
import com.ringme.cms.repository.gym.MemberSubscriptionRepository;
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
    private final MemberSubscriptionRepository memberSubscriptionRepository;

    public List<AjaxSearchDto> ajaxSearchMembership(String input, Long id) {
        List<MemberSubscription> memberSubscriptions = memberSubscriptionRepository.findByMemberIdAndType(id, 0);
        Integer type = null;
        for (MemberSubscription memberSubscription : memberSubscriptions) {
            if(memberSubscription.getIsRecurring() == 1){
                type = 1;
            }
        }
        return Helper.listAjax(membershipRepository.ajaxSearchMembership(Helper.processStringSearch(input), type));
    }
}
