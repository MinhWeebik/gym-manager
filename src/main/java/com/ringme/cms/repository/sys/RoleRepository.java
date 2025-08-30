package com.ringme.cms.repository.sys;

import com.ringme.cms.model.sys.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    @Query(value = "SELECT * FROM tbl_role " +
            "WHERE ?1 is null OR role_name LIKE CONCAT('%', ?1, '%') OR description LIKE CONCAT('%', ?1, '%')" +
            "order by created_at desc",
            countQuery = "SELECT COUNT(*) FROM tbl_role " +
                    "WHERE ?1 is null OR role_name LIKE CONCAT('%', ?1, '%') OR description LIKE CONCAT('%', ?1, '%')", nativeQuery = true)
    Page<Role> search(String search, Pageable pageable);

    @Query(value = "Select r.* from tbl_role r where r.id not in (?1)", nativeQuery = true)
    List<Role> findAllRoleNotInListIdRole(List<Long> id);

    @Query(value = """
        select r.* from tbl_user_role ur 
            inner join tbl_role r on ur.role_id = r.id 
        where ur.user_id = ?1
    """, nativeQuery = true)
    List<Role> findAllRoleOwnedByUserId(Long userId);

    @Query(value = "select * from tbl_role where role_name = ?1", nativeQuery = true)
    Role findIdByRoleName(String roleName);
}
