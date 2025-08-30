package com.ringme.cms.service.sys;

import com.ringme.cms.dto.sys.UserSecurity;
import com.ringme.cms.model.sys.UserRole;
import com.ringme.cms.repository.sys.UserRoleRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Log4j2
@Service
@Transactional
public class UserRoleServiceImpl implements UserRoleService {
    @Autowired
    UserRoleRepository userRoleRepository;

    @Override
    public void deleteUserRole(Long userId, List<Long> roleIds) throws Exception {
        try {
            userRoleRepository.deleteUserRole(userId, roleIds);
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            throw new Exception(e);
        }
    }

    @Override
    public void createUserRole(Long userId, List<Long> roleIds) throws Exception {
        try {
            UserSecurity userSecurity = (UserSecurity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            List<UserRole> userRoles = new ArrayList<>();
            for (Long roleId : roleIds) {
                UserRole userRole = new UserRole();
                userRole.setUserId(userId);
                userRole.setRoleId(roleId);
                userRole.setCreatedBy(userSecurity.getId());
                userRole.setUpdatedBy(userSecurity.getId());
                userRoles.add(userRole);
            }
            userRoleRepository.saveAll(userRoles);
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            throw new Exception(e);
        }
    }
}
