package com.ringme.cms.service.sys;


import com.google.gson.JsonObject;
import com.ringme.cms.common.Helper;
import com.ringme.cms.dto.sys.RouterFormDto;
import com.ringme.cms.dto.sys.UserSecurity;
import com.ringme.cms.model.sys.Router;
import com.ringme.cms.repository.sys.RouterRepository;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Log4j2
@Service
@Transactional
public class RouterServiceImpl implements RouterService {
    @Autowired
    RouterRepository routerRepository;
    @Autowired
    ModelMapper modelMapper;

    @Override
    public Page<Router> page(int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize);
        return routerRepository.search(pageable);
    }

    @Override
    public void save(RouterFormDto routerFormDto) throws Exception {
        try {
            UserSecurity userSecurity = (UserSecurity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Router router;

            if (routerFormDto.getId() == null) {
                router = modelMapper.map(routerFormDto, Router.class);
                router.setCreatedBy(userSecurity.getId());
                router.setUpdatedBy(userSecurity.getId());
                router.setActive(true);
            } else {
                router = routerRepository.findById(routerFormDto.getId())
                        .orElseThrow(() -> new RuntimeException("Router with ID " + routerFormDto.getId() + " not found"));
                modelMapper.map(routerFormDto, router);
                router.setUpdatedBy(userSecurity.getId());
            }

            routerRepository.save(router);
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public JsonObject updateStatus(boolean status, Long id) throws Exception {
        JsonObject jsonObject = new JsonObject();
        try {
            jsonObject.addProperty("code", 200);
            jsonObject.addProperty("status", true);
            jsonObject.addProperty("msg", "Updated status!");
            if (routerRepository.updateStatus(status, id) <= 0) {
                jsonObject.addProperty("status", false);
                jsonObject.addProperty("msg", "Update failed!");
            }
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
        return jsonObject;
    }

    @Override
    public List<Router> findAllRouterUnActive() {
        return routerRepository.findAllRouterUnActive();
    }

    @Override
    public List<Router> findAllRouterActive() {
        return routerRepository.findAllRouterActive();
    }

    @Override
    public List<Router> findAllRouterNotInRole(List<Long> roleIds) {
        return routerRepository.findAllRouterNotInRole(roleIds);
    }

    @Override
    public Optional<Router> findRouterById(Long id) {
        return routerRepository.findById(id);
    }

    @Override
    public void deleteById(Long id) {
        routerRepository.deleteById(id);
    }

    @Override
    public List<Map<String, String>> ajaxSearch(String input) {
        List<Router> list = routerRepository.ajaxSearch(Helper.processStringSearch(input));

        return list.stream().map(item -> {
            Map<String, String> map = new HashMap<>();
            map.put("id", item.getId().toString());
            map.put("text", item.getDes() + " : " + item.getRouterLink());
            return map;
        }).collect(Collectors.toList());
    }
}
