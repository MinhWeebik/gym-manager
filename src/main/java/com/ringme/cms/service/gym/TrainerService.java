package com.ringme.cms.service.gym;

import com.ringme.cms.common.Helper;
import com.ringme.cms.dto.AjaxSearchDto;
import com.ringme.cms.model.gym.MemberSubscription;
import com.ringme.cms.model.gym.Trainer;
import com.ringme.cms.repository.gym.TrainerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class TrainerService {

    private final TrainerRepository trainerRepository;

    public List<AjaxSearchDto> ajaxSearchTrainer(String input) {
        return Helper.listAjax(trainerRepository.ajaxSearchTrainer(Helper.processStringSearch(input)),1);
    }
}
