package com.ringme.cms.repository.gym;

import com.ringme.cms.model.gym.Product;
import com.ringme.cms.model.gym.ProductCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {
    @Query(value = "SELECT * FROM product_category WHERE status = 1 ORDER BY name ASC", nativeQuery = true)
    List<ProductCategory> getAll();

    @Query(value = "SELECT c.id AS `id`, c.name AS `name` " +
            "FROM product_category c " +
            "WHERE c.status = 1 " +
            "AND (:input IS NULL OR (c.id = :input OR c.name LIKE CONCAT('%', :input, '%'))) " +
            "LIMIT 20", nativeQuery = true)
    List<String[]> ajaxSearchCategory(@Param("input") String input);

    @Query(value = "SELECT * FROM product_category " +
            "WHERE (LOWER(:name) IS NULL OR name LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND ((:status is null and status != 0) OR status = :status) " +
            "ORDER BY created_at DESC",
            countQuery = "SELECT COUNT(*) FROM product " +
                    "WHERE (LOWER(:name) IS NULL OR name LIKE LOWER(CONCAT('%', :name, '%'))) " +
                    "AND ((:status is null and status != 0) OR status = :status) ",nativeQuery = true)
    Page<ProductCategory> getPage(@Param("name") String name,
                          @Param("status") Integer status, Pageable pageable);


}
