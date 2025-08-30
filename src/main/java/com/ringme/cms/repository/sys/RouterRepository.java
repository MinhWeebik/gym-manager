package com.ringme.cms.repository.sys;

import com.ringme.cms.model.sys.Router;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface RouterRepository extends PagingAndSortingRepository<Router, Long> {
    @Query(value = "SELECT * from tbl_router r where r.active = 1", nativeQuery = true)
    List<Router> findAllRouterActive();

    @Query(value = "SELECT * from tbl_router r where r.active = 0", nativeQuery = true)
    List<Router> findAllRouterUnActive();

    @Modifying
    @Query(value = "UPDATE tbl_router r set r.active = ?1, r.updated_at = now() where r.id = ?2", nativeQuery = true)
    int updateStatus(boolean status, Long id);

    @Query(value = "SELECT r.* from tbl_router r where r.id not in (?1) and r.active =1", nativeQuery = true)
    List<Router> findAllRouterNotInRole(List<Long> routerIds);

    @Query(value = "SELECT r.* from tbl_router r where r.id in (?1) and r.active =1", nativeQuery = true)
    List<Router> findAllRouterByListId(List<Long> id);

    @Query(value = "SELECT * FROM tbl_router order by created_at desc",
            countQuery = "SELECT COUNT(*) FROM tbl_router", nativeQuery = true)
    Page<Router> search(Pageable pageable);

    @Query(value = "SELECT * FROM tbl_router " +
            "WHERE (:input is null or (id = :input OR des LIKE CONCAT('%', :input, '%') " +
            "OR router_link LIKE CONCAT('%', :input, '%'))) AND active = 1", nativeQuery = true)
    List<Router> ajaxSearch(@Param("input") String input);
}
