package com.ringme.cms.service.gym;

import com.ringme.cms.common.Helper;
import com.ringme.cms.dto.AjaxSearchDto;
import com.ringme.cms.dto.gym.ClassDto;
import com.ringme.cms.model.gym.Attendance;
import com.ringme.cms.model.gym.Classes;
import com.ringme.cms.model.gym.MemberSubscription;
import com.ringme.cms.model.gym.Membership;
import com.ringme.cms.repository.gym.AttendanceRepository;
import com.ringme.cms.repository.gym.ClassRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Log4j2
public class ClassService {

    private final ClassRepository classRepository;

    private final ModelMapper modelMapper;

    private final AttendanceRepository attendanceRepository;

    public List<AjaxSearchDto> ajaxSearchClass(String input) {
        return Helper.listAjax(classRepository.ajaxSearchClass(Helper.processStringSearch(input)),1);
    }

    public Page<Classes> getPage(String name, Integer status, Integer pageNo, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize);
        return classRepository.getAll(name, status, pageable);
    }

    public void save(ClassDto formDto) throws Exception {
        try {
            Classes classes;
            if (formDto.getId() == null) {
                classes = modelMapper.map(formDto, Classes.class);
                classes.setCreatedAt(LocalDateTime.now());
                classes.setUpdatedAt(LocalDateTime.now());
                classes.setStatus(1);
            } else {
                classes = classRepository.findById(formDto.getId()).orElseThrow();
                modelMapper.map(formDto, classes);
                classes.setUpdatedAt(LocalDateTime.now());
            }
            classRepository.save(classes);
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public void softDelete(Long id) throws Exception{
        Classes classes = classRepository.findById(id).orElseThrow(() -> new Exception("Không tìm thấy lớp"));
        List<Attendance> memberSubscriptions = attendanceRepository.findByClassId(classes.getId());
        if(!memberSubscriptions.isEmpty())
        {
            throw new Exception("Đang có thành viên đăng ký lớp này");
        }
        classes.setStatus(0);
        classRepository.save(classes);
    }
}
