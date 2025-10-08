package com.ringme.cms.repository.gym;

import com.ringme.cms.model.gym.ProductOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductOrderItemRepository extends JpaRepository<ProductOrderItem,Long> {
    @Query(value = "SELECT poi.* FROM product_order_item poi INNER JOIN product p ON p.id = poi.product_id WHERE poi.order_id = (SELECT id FROM product_order ORDER BY order_time DESC LIMIT 1)", nativeQuery = true)
    List<ProductOrderItem> findByPreviousOrder();
}
