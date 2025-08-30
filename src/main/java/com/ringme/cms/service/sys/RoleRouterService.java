package com.ringme.cms.service.sys;


import com.ringme.cms.model.sys.RouterRole;

import java.util.List;

public interface RoleRouterService {
    List<RouterRole> findAllRouterRoleByRoleId(Long roleId);

    List<RouterRole> findAllRouterRoleByListRoleId(List<Long> roleIds);

    void deleteRoleRouter(Long roleId, List<Long> routerIds) throws Exception;

    void createRoleRouter(Long roleId, List<Long> routerIds) throws Exception;
}
