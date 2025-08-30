package com.ringme.cms.repository.sys;

import com.ringme.cms.model.sys.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    @Query(value = "Select * from tbl_user_role ur inner join tbl_role r on r.id = ur.role_id " +
            "inner join tbl_user u on u.id = ur.user_id where ur.user_id =?1", nativeQuery = true)
    List<UserRole> findUserRoleByUserId(Long id);

    @Modifying
    @Query(value = "DELETE FROM tbl_user_role WHERE user_id = ?1 AND role_id IN (?2)", nativeQuery = true)
    void deleteUserRole(Long userId, List<Long> roleIds);

    @Query(value = "Select * FROM tbl_user_role WHERE user_id = ?1 AND role_id = ?2", nativeQuery = true)
    Optional<UserRole> findByUserIdAndRoleId(Long userId, Long roleId);
}
