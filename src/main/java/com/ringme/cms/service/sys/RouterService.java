package com.ringme.cms.service.sys;


import com.google.gson.JsonObject;
import com.ringme.cms.dto.sys.RouterFormDto;
import com.ringme.cms.model.sys.Router;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface RouterService {
    Page<Router> page(int pageNo, int pageSize);

    void save(RouterFormDto routerFormDto) throws Exception;

    JsonObject updateStatus(boolean status, Long id) throws Exception;

    List<Router> findAllRouterUnActive();

    List<Router> findAllRouterActive();

    List<Router> findAllRouterNotInRole(List<Long> roleIds);

    Optional<Router> findRouterById(Long id);

    void deleteById(Long id);

    List<Map<String, String>> ajaxSearch(String input);
}
