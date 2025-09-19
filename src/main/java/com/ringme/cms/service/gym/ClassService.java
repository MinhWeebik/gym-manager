package com.ringme.cms.service.gym;

import com.ringme.cms.common.Helper;
import com.ringme.cms.dto.AjaxSearchDto;
import com.ringme.cms.repository.gym.ClassRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class ClassService {

    private final ClassRepository classRepository;

    public List<AjaxSearchDto> ajaxSearchClass(String input) {
        return Helper.listAjax(classRepository.ajaxSearchClass(Helper.processStringSearch(input)),1);
    }
}
