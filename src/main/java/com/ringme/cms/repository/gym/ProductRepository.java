package com.ringme.cms.repository.gym;

import com.ringme.cms.model.gym.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product,Long> {
    @Query(value = "SELECT * FROM product WHERE status = 1 AND category_id = :id", nativeQuery = true)
    List<Product> findByCategoryId(@Param("id") Long id);

    @Query(value = "SELECT * FROM product p WHERE p.status = 1 AND LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))", nativeQuery = true)
    List<Product> findByName(@Param("name") String name);

}
