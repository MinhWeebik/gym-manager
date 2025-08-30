package com.ringme.cms.repository.sys;


import com.ringme.cms.model.sys.Menu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface MenuRepository extends PagingAndSortingRepository<Menu, Long> {
    @Query(value = "SELECT * FROM tbl_menu " +
            "where (:id is null or id = :id) or (:id is null or parent_name_id = :id) " +
            "ORDER BY created_at DESC",
            countQuery = "SELECT count(*) FROM tbl_menu " +
                    "where (:id is null or id = :id) or (:id is null or parent_name_id = :id) " +
                    "ORDER BY created_at DESC",
            nativeQuery = true)
    Page<Menu> getPage(@Param("id") Long id, Pageable pageable);

    @Query(value = "SELECT name_en FROM tbl_menu where id = :id order by order_num", nativeQuery = true)
    String getNameEn(@Param("id") Long id);


    @Query(value = "SELECT * FROM tbl_menu order by order_num", nativeQuery = true)
    List<Menu> findAll();

    List<Menu> findByParentNameIsNullOrderByOrderNumAsc();

    @Query(value = "SELECT * FROM tbl_menu where parent_name_id = :parentId order by order_num", nativeQuery = true)
    List<Menu> findByParentNameId(@Param("parentId") Long parentId);

    @Override
    List<Menu> findAllById(Iterable<Long> longs);

    @Override
    <S extends Menu> S save(S entity);

    @Override
    void deleteById(Long aLong);

    @Override
    void delete(Menu entity);

    @Query(value = "SELECT id AS `id`, name_en AS `text` FROM tbl_menu " +
            "WHERE (:input is null or (id = :input OR name_en LIKE CONCAT('%', :input, '%'))) LIMIT 20", nativeQuery = true)
    List<String[]> ajaxSearch(@Param("input") String input);

    @Query(value = "SELECT * FROM tbl_menu " +
            "WHERE (:input is null or (id = :input OR name_en LIKE CONCAT('%', :input, '%'))) LIMIT 20", nativeQuery = true)
    List<Menu> ajaxSearch2(@Param("input") String input);
}
