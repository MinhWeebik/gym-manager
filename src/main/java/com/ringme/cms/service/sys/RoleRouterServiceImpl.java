package com.ringme.cms.service.sys;


import com.ringme.cms.dto.sys.UserSecurity;
import com.ringme.cms.model.sys.RouterRole;
import com.ringme.cms.repository.sys.RouterRoleRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Log4j2
@Transactional
@Service
public class RoleRouterServiceImpl implements RoleRouterService {
    @Autowired
    RouterRoleRepository routerRoleRepository;

    @Override
    public List<RouterRole> findAllRouterRoleByRoleId(Long roleId) {
        return routerRoleRepository.findAllRouterRoleByRoleId(roleId);
    }

    @Override
    public List<RouterRole> findAllRouterRoleByListRoleId(List<Long> roleIds) {
        return routerRoleRepository.findAllRouterRoleByListRoleId(roleIds);
    }

    @Override
    public void deleteRoleRouter(Long roleId, List<Long> routerIds) throws Exception {
        try {
            routerRoleRepository.deleteRouterRole(roleId, routerIds);
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            throw new Exception(e);
        }
    }

    @Override
    public void createRoleRouter(Long roleId, List<Long> routerIds) throws Exception {
        try {
            UserSecurity userSecurity = (UserSecurity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            List<RouterRole> routerRoles = new ArrayList<>();
            for (Long routerId : routerIds) {
                RouterRole routerRole = new RouterRole();
                routerRole.setRouterId(routerId);
                routerRole.setRoleId(roleId);
                routerRole.setCreatedBy(userSecurity.getId());
                routerRole.setUpdatedBy(userSecurity.getId());
                routerRoles.add(routerRole);
            }
            routerRoleRepository.saveAll(routerRoles);
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            throw new Exception(e);
        }
    }
}
