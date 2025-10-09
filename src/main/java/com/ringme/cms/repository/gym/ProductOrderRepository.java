package com.ringme.cms.repository.gym;

import com.ringme.cms.model.gym.ProductOrder;
import com.ringme.cms.model.gym.ProductOrderItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductOrderRepository extends JpaRepository<ProductOrder,Long> {

    @Query(value = "SELECT SUM(poi.quantity) AS total FROM product_order po INNER JOIN product_order_item poi ON poi.order_id = po.id WHERE po.status = 1 AND MONTH(po.order_time) = MONTH(CURDATE())" ,nativeQuery = true)
    Integer getThisMonthSoldData();

    @Query(value = "SELECT SUM(p.price * poi.quantity) AS total FROM product_order po INNER JOIN product_order_item poi ON poi.order_id = po.id INNER JOIN product p ON p.id = poi.product_id \n" +
            "WHERE po.status = 1 AND p.id = :productId AND MONTH(po.order_time) = MONTH(CURDATE())", nativeQuery = true)
    BigDecimal getMonthlyDonutData(@Param("productId") Long productId);

    @Query(value = "SELECT SUM(p.price * poi.quantity) AS total FROM product_order po INNER JOIN product_order_item poi ON poi.order_id = po.id INNER JOIN product p ON p.id = poi.product_id \n" +
            "WHERE po.status = 1 AND p.id = :productId AND YEAR(po.order_time) = YEAR(CURDATE())", nativeQuery = true)
    BigDecimal getYearlyDonutData(@Param("productId") Long productId);


}
