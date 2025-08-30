package com.ringme.cms.service.sys;

import com.ringme.cms.common.Helper;
import com.ringme.cms.dto.AjaxSearchDto;
import com.ringme.cms.dto.sys.MenuFormDto;
import com.ringme.cms.dto.sys.RouterFormDto;
import com.ringme.cms.dto.sys.UserSecurity;
import com.ringme.cms.model.sys.Menu;
import com.ringme.cms.model.sys.Router;
import com.ringme.cms.repository.sys.IconRepository;
import com.ringme.cms.repository.sys.MenuRepository;
import com.ringme.cms.repository.sys.RouterRepository;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@Log4j2
public class MenuServiceImpl implements MenuService {
    @Autowired
    MenuRepository menuRepository;
    @Autowired
    RouterRepository routerRepository;
    @Autowired
    IconRepository iconRepository;
    @Autowired
    ModelMapper modelMapper;

    @Override
    public Page<Menu> getPage(Long parentName, int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, Sort.by("parent_name_id").ascending()
                .and(Sort.by("order_num").ascending()));
        return menuRepository.getPage(parentName, pageable);
    }

    @Override
    public Optional<Menu> findMenuById(Long id) {
        return menuRepository.findById(id);
    }

    @Override
    public void save(MenuFormDto formDto) throws Exception {
        try {
            UserSecurity userSecurity = (UserSecurity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            routerRepository.findById(formDto.getRouterId()).orElseThrow();
            iconRepository.findById(formDto.getIconId()).orElseThrow();
            if (formDto.getParentNameId() != null)
                menuRepository.findById(formDto.getParentNameId()).orElseThrow();
            Menu menu;

            if (formDto.getId() == null) {
                menu = modelMapper.map(formDto, Menu.class);
                menu.setCreatedBy(userSecurity.getId());
                menu.setUpdatedBy(userSecurity.getId());
            } else {
                menu = menuRepository.findById(formDto.getId()).orElseThrow();
                modelMapper.map(formDto, menu);
                menu.setUpdatedBy(userSecurity.getId());
            }

            menuRepository.save(menu);
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteById(Long id) {
        menuRepository.deleteById(id);
    }

    @Override
    public List<Menu> getListMenuNoParent() {
        List<Menu> list = menuRepository.findByParentNameIsNullOrderByOrderNumAsc();
        list.sort(Comparator.comparing(Menu::getOrderNum));
        return list;
    }

    @Override
    public List<AjaxSearchDto> ajaxSearch(String input) {
        return Helper.listAjax(menuRepository.ajaxSearch(Helper.processStringSearch(input)), 1);
    }

    @Override
    public List<Map<String, String>> ajaxSearch2(String input) {
        List<Menu> list = menuRepository.ajaxSearch2(Helper.processStringSearch(input));

        return list.stream()
                .map(menu -> {
                    Map<String, String> map = new HashMap<>();
                    map.put("id", String.valueOf(menu.getId()));
                    map.put("text", menu.getNameEn() + " : " + menu.getRouter().getRouterLink());
                    return map;
                })
                .collect(Collectors.toList());
    }
}
