package com.ringme.cms.service.sys;

import java.util.List;

public interface UserRoleService {
    void deleteUserRole(Long userId, List<Long> roleIds) throws Exception;

    void createUserRole(Long userId, List<Long> roleIds) throws Exception;
}
