package com.ringme.cms.service.sys;

import com.google.gson.JsonObject;
import com.ringme.cms.common.Helper;
import com.ringme.cms.dto.AjaxSearchDto;
import com.ringme.cms.dto.sys.UserFormDto;
import com.ringme.cms.dto.sys.UserProfileDto;
import com.ringme.cms.dto.sys.UserSecurity;
import com.ringme.cms.model.sys.User;
import com.ringme.cms.repository.sys.RoleRepository;
import com.ringme.cms.repository.sys.UserRepository;
import com.ringme.cms.repository.sys.UserRoleRepository;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Log4j2
@Transactional
public class UserServiceImpl implements UserService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    UserRoleRepository userRoleRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    public Page<User> getPage(int pageNo, int pageSize, String username, String fullname, String phone, String email) {
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize);
//        if (username != null && username.trim().isEmpty()) username = null;
//        if (fullname != null && fullname.trim().isEmpty()) fullname = null;
//        if (phone != null && phone.trim().isEmpty()) phone = null;
//        if (email != null && email.trim().isEmpty()) email = null;
        return userRepository.getPage(username, fullname, phone, email, pageable);
    }

    @Override
    public Optional<User> findByIdUser(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public void save(UserFormDto formDto) throws Exception {
        try {
            UserSecurity userSecurity = (UserSecurity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            User user;
            String password = formDto.getPassword();

            if (formDto.getId() == null) {
                user = modelMapper.map(formDto, User.class);
                user.setCreatedBy(userSecurity.getId());
                user.setUpdatedBy(userSecurity.getId());
            } else {
                user = userRepository.findById(formDto.getId()).orElseThrow();
                modelMapper.map(formDto, user);
                user.setUpdatedBy(userSecurity.getId());
            }

            if (password != null && !password.isEmpty()) {
                user.setPassword(passwordEncoder.encode(password));
            }

            userRepository.save(user);
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteUser(Long id) throws Exception {
        try {
            User user = userRepository.findById(id).orElseThrow();
            user.setActive(2);
            userRepository.save(user);
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            throw new Exception(e);
        }
    }

    @Override
    public JsonObject updateStatus(boolean status, Long id) throws Exception {
        JsonObject jsonObject = new JsonObject();
        try {
            jsonObject.addProperty("code", 200);
            jsonObject.addProperty("status", true);
            jsonObject.addProperty("msg", "Updated status!");
            if (userRepository.updateStatus(status, new Date(), id) <= 0) {
                jsonObject.addProperty("status", false);
                jsonObject.addProperty("msg", "Update failed!");
            }
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
        return jsonObject;
    }

    @Override
    public void updateProfile(UserProfileDto formDto) throws Exception {
        try {
            UserSecurity userSecurity = (UserSecurity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String password = formDto.getPassword();

            User user = userRepository.findById(userSecurity.getId()).orElseThrow();
            modelMapper.map(formDto, user);
            if (password != null && !password.isEmpty()) {
                user.setPassword(passwordEncoder.encode(password));
            }

            userRepository.save(user);
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return ((UserDetails) authentication.getPrincipal()).getUsername();
    }

    @Override
    public Long getId() {
        return userRepository.getId(getUsername());
    }

    @Override
    public User getUser() {
        return userRepository.getUser(getUsername());
    }

    @Override
    public Integer getPartnerId() {
        return userRepository.getPartnerId(getUsername());
    }

    @Override
    public List<AjaxSearchDto> ajaxSearchCreated(String input) {
        return Helper.listAjax(userRepository.ajaxSearchCreated(Helper.processStringSearch(input)), 1);
    }
}
