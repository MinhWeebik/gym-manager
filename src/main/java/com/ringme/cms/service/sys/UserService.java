package com.ringme.cms.service.sys;

import com.google.gson.JsonObject;
import com.ringme.cms.dto.AjaxSearchDto;
import com.ringme.cms.dto.sys.UserFormDto;
import com.ringme.cms.dto.sys.UserProfileDto;
import com.ringme.cms.model.sys.User;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface UserService {
    Page<User> getPage(int pageNo, int pageSize, String username, String fullname, String phone, String email);

    Optional<User> findByIdUser(Long id);

    void save(UserFormDto formDto) throws Exception;

    void deleteUser(Long id) throws Exception;

    JsonObject updateStatus(boolean status, Long id) throws Exception;

    void updateProfile(UserProfileDto formDto) throws Exception;

    String getUsername();

    User getUser();

    Long getId();

    Integer getPartnerId();

    List<AjaxSearchDto> ajaxSearchCreated(String input);
}
