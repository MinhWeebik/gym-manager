package com.ringme.cms.repository.sys;

import com.ringme.cms.model.sys.Icon;
import com.ringme.cms.model.sys.Router;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IconRepository extends JpaRepository<Icon, Long> {
    @Query(value = "SELECT * FROM tbl_icon " +
            "WHERE (:input is null or (id = :input OR display_name LIKE CONCAT('%', :input, '%') OR name LIKE CONCAT('%', :input, '%')))", nativeQuery = true)
    List<Icon> ajaxSearch(@Param("input") String input);
}
