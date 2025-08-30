package com.ringme.cms.service.sys;


import com.ringme.cms.dto.sys.RoleFormDto;
import com.ringme.cms.dto.sys.UserSecurity;
import com.ringme.cms.model.sys.Role;
import com.ringme.cms.repository.sys.RoleRepository;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Log4j2
@Service
@Transactional
public class RoleServiceImpl implements RoleService {
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    private ModelMapper modelMapper;

    @Override
    public Page<Role> page(int pageNo, int pageSize, String search) {
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize);
        return roleRepository.search(search, pageable);
    }

    @Override
    public void save(RoleFormDto formDto) throws Exception {
        try {
            UserSecurity userSecurity = (UserSecurity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Role role;

            if (formDto.getId() == null) {
                role = modelMapper.map(formDto, Role.class);
                role.setCreatedBy(userSecurity.getId());
                role.setUpdatedBy(userSecurity.getId());
            } else {
                role = roleRepository.findById(formDto.getId()).orElseThrow();
                modelMapper.map(formDto, role);
                role.setUpdatedBy(userSecurity.getId());
            }

            roleRepository.save(role);
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteById(Long id) {
        roleRepository.deleteById(id);
    }

    @Override
    public List<Role> findAllRole() {
        return roleRepository.findAll();
    }

    @Override
    public Optional<Role> findRoleById(Long id) {
        return roleRepository.findById(id);
    }

    @Override
    public List<Role> findAllRoleNotInListIdRole(List<Long> idRole) {
        return roleRepository.findAllRoleNotInListIdRole(idRole);
    }
}
