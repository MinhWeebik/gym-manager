package com.ringme.cms.service.sys;

import com.ringme.cms.dto.sys.UserSecurity;
import com.ringme.cms.model.sys.Router;
import com.ringme.cms.model.sys.User;
import com.ringme.cms.model.sys.UserRole;
import com.ringme.cms.repository.sys.UserRepository;
import com.ringme.cms.repository.sys.UserRoleRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Log4j2
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    UserRoleRepository userRoleRepository;
    @Autowired
    RoleRouterService roleRouterService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> userOpt = userRepository.findUserByUserName(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            log.info("User login: {}", user.getUsername());

            // Lấy các quyền
            List<UserRole> userRoles = userRoleRepository.findUserRoleByUserId(user.getId());
            log.info("SIZE: {}", userRoles.size());
            List<GrantedAuthority> grandList = userRoles.stream()
                    .map(e -> new SimpleGrantedAuthority("ROLE_" + e.getRole().getRoleName()))
                    .collect(Collectors.toList());

            // Lấy các đường dẫn user được phép truy cập
            List<Long> roleIds = userRoles.stream().map(e -> e.getRole().getId()).collect(Collectors.toList());
            Set<String> routerLink = roleRouterService.findAllRouterRoleByListRoleId(roleIds).stream()
                    .map(e -> {
                        Router router = e.getRouter();
                        if (router != null && router.isActive()) {
                            return router.getRouterLink();
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            // Xử lý kiểu mã hóa cũ
            String passwordHashed = user.getPassword();
            if (passwordHashed != null && passwordHashed.startsWith("$2y$")) {
                passwordHashed = passwordHashed.replace("$2y$", "$2a$");
            }

            UserSecurity userSercurity = new UserSecurity(user.getUsername(), user.getPassword(), user.getActive() == 1, grandList);
            userSercurity.setId(user.getId());
            userSercurity.setFullname(user.getFullname());
            userSercurity.setEmail(user.getEmail());
            userSercurity.setPhone(user.getPhone());
            userSercurity.setCreatedAt(user.getCreatedAt());
            userSercurity.setCreatedBy(user.getCreatedBy());
            userSercurity.setUpdatedAt(user.getUpdatedAt());
            userSercurity.setUpdatedBy(user.getUpdatedBy());
            userSercurity.setRouter(routerLink);
            return userSercurity;
        } else {
            throw new UsernameNotFoundException("User " + username + " was not found in database");
        }
    }
}
