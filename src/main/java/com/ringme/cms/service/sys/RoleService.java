package com.ringme.cms.service.sys;

import com.ringme.cms.dto.sys.RoleFormDto;
import com.ringme.cms.dto.sys.RouterFormDto;
import com.ringme.cms.model.sys.Role;
import com.ringme.cms.model.sys.Router;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface RoleService {
    Page<Role> page(int pageNo, int pageSize, String search);

    void save(RoleFormDto formDto) throws Exception;

    void deleteById(Long id);

    List<Role> findAllRole();

    Optional<Role> findRoleById(Long id);

    List<Role> findAllRoleNotInListIdRole(List<Long> idRole);
}
