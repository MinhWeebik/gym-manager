package com.ringme.cms.repository.gym;

import com.ringme.cms.model.gym.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {
    @Query(value = "SELECT * FROM product_category WHERE status = 1 ORDER BY name ASC", nativeQuery = true)
    List<ProductCategory> getAll();
}
