package com.ringme.cms.repository.gym;

import com.ringme.cms.model.gym.ProductOrder;
import com.ringme.cms.model.gym.ProductOrderItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductOrderRepository extends JpaRepository<ProductOrder,Long> {

}
