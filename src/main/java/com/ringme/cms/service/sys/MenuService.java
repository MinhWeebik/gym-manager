package com.ringme.cms.service.sys;


import com.ringme.cms.dto.AjaxSearchDto;
import com.ringme.cms.dto.sys.MenuFormDto;
import com.ringme.cms.model.sys.Menu;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface MenuService {
    Page<Menu> getPage(Long parentName, int pageNo, int pageSize);

    Optional<Menu> findMenuById(Long id);

    void save(MenuFormDto formDto) throws Exception;

    void deleteById(Long id);

    List<Menu> getListMenuNoParent();

    List<AjaxSearchDto> ajaxSearch(String input);

    List<Map<String, String>> ajaxSearch2(String input);
}
