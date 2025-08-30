package com.ringme.cms.repository.sys;

import com.ringme.cms.model.sys.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query(value = """
                select * from tbl_user 
                where (:username IS NULL OR username like concat('%', :username, '%'))
                    and (:fullname IS NULL OR fullname like concat('%', :fullname, '%'))     
                    and (:phone IS NULL OR phone like concat('%', :phone, '%'))     
                    and (:email IS NULL OR email like concat('%', :email, '%'))
                    and active != 2 order by created_at desc
            """,
            countQuery = """
                        select count(*) from tbl_user 
                        where (:username IS NULL OR username like concat('%', :username, '%'))
                            and (:fullname IS NULL OR fullname like concat('%', :fullname, '%'))     
                            and (:phone IS NULL OR phone like concat('%', :phone, '%'))     
                            and (:email IS NULL OR email like concat('%', :email, '%'))
                            and active != 2
                    """,
            nativeQuery = true)
    Page<User> getPage(@Param("username") String username,
                       @Param("fullname") String fullname,
                       @Param("phone") String phone,
                       @Param("email") String email,
                       Pageable pageable);

    @Modifying
    @Query(value = "UPDATE tbl_user set active = ?1, updated_at = ?2 where id = ?3", nativeQuery = true)
    int updateStatus(boolean active, Date updatedAt, Long id);


    @Query(value = "Select * from tbl_user u where u.username = ?1", nativeQuery = true)
    Optional<User> findUserByUserName(String username);

    @Query(value = "SELECT * FROM tbl_user l WHERE (:id IS NULL OR l.id = :id)" +
            "AND (:email IS NULL OR l.email like %:email%) " +
            "AND (:fullname IS NULL OR l.fullname like %:fullname%) " + "AND (:phone IS NULL OR l.phone like %:phone%) " +
            "AND (l.active = 1) order by l.id desc ",
            countQuery = "SELECT count(*) FROM tbl_user l WHERE (:id IS NULL OR l.id = :id)" +
                    "AND (:email IS NULL OR l.email like %:email%) " +
                    "AND (:fullname IS NULL OR l.fullname like %:fullname%) " + "AND (:phone IS NULL OR l.phone like %:phone%) " +
                    "AND (l.active = 1) order by l.id desc ", nativeQuery = true)
    Page<User> search(@Param("id") Long id, @Param("email") String email, @Param("fullname") String fullname, @Param("phone") String phone, Pageable pageable);

    @Query(value = "SELECT id FROM tbl_user where username = :username", nativeQuery = true)
    String checkId(@Param("username") String username);

    @Query(value = "SELECT id FROM tbl_user where username = :username", nativeQuery = true)
    Long getId(@Param("username") String username);

    @Query(value = "SELECT * FROM tbl_user where username = :username", nativeQuery = true)
    User getUser(@Param("username") String username);

    @Query(value = "SELECT partner_id FROM tbl_user where username = :username", nativeQuery = true)
    Integer getPartnerId(@Param("username") String username);

    @Query(value = "SELECT id AS `id`, username AS `text` FROM tbl_user " +
            "WHERE (:input is null or (id = :input OR username LIKE CONCAT('%', :input, '%'))) AND active = 1 ORDER BY username DESC LIMIT 20", nativeQuery = true)
    List<String[]> ajaxSearchCreated(@Param("input") String input);
}
