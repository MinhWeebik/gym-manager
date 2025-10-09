package com.ringme.cms.repository.gym;

import com.ringme.cms.model.gym.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface ProductRepository extends JpaRepository<Product,Long> {
    @Query(value = "SELECT * FROM product WHERE status = 1 AND category_id = :id", nativeQuery = true)
    List<Product> findByCategoryId(@Param("id") Long id);

    @Query(value = "SELECT * FROM product p WHERE p.status = 1 AND LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')) AND p.category_id IS NOT NULL", nativeQuery = true)
    List<Product> findByName(@Param("name") String name);

    @Query(value = "SELECT p.* FROM product_order_item poi INNER JOIN product p ON p.id = poi.product_id WHERE poi.order_id = (SELECT id FROM product_order ORDER BY order_time DESC LIMIT 1)", nativeQuery = true)
    List<Product> findByPreviousOrder();

    @Query(value = "SELECT * FROM product " +
            "WHERE (LOWER(:name) IS NULL OR name LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND ((:status is null and status != 0) OR status = :status) " +
            "AND (:category is null OR category_id = :category) " +
            "ORDER BY created_at DESC",
            countQuery = "SELECT COUNT(*) FROM product " +
                    "WHERE (LOWER(:name) IS NULL OR name LIKE LOWER(CONCAT('%', :name, '%'))) " +
                    "AND ((:status is null and status != 0) OR status = :status) " +
                    "AND (:category is null OR category_id = :category)",nativeQuery = true)
    Page<Product> getPage(@Param("name") String name,
                          @Param("status") Integer status,
                          @Param("category") Long category, Pageable pageable);

    @Query(value = "SELECT * FROM product " +
            "WHERE (LOWER(:name) IS NULL OR name LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND ((:status is null and status != 0) OR status = :status) " +
            "AND id NOT IN (:exceptIds) " +
            "ORDER BY created_at DESC",
            countQuery = "SELECT COUNT(*) FROM product " +
                    "WHERE (LOWER(:name) IS NULL OR name LIKE LOWER(CONCAT('%', :name, '%'))) " +
                    "AND ((:status is null and status != 0) OR status = :status) " +
                    "AND id NOT IN (:exceptIds)",nativeQuery = true)
    Page<Product> search(@Param("name") String name,
                         @Param("status") Integer status,
                         @Param("exceptIds") List<Long> exceptIds,
                         Pageable pageable);

    @Query(value = "SELECT * FROM product WHERE category_id IS NOT NULL", nativeQuery = true)
    Set<Long> findIdByCategoryId();

    @Query(value = "SELECT * FROM product WHERE status = 1", nativeQuery = true)
    List<Product> getAllProduct();
}
