package com.ringme.cms.service.gym;

import com.ringme.cms.dto.gym.PreviousOrderDto;
import com.ringme.cms.dto.gym.ProductDto;
import com.ringme.cms.model.gym.Product;
import com.ringme.cms.model.gym.ProductOrder;
import com.ringme.cms.model.gym.ProductOrderItem;
import com.ringme.cms.repository.gym.ProductOrderItemRepository;
import com.ringme.cms.repository.gym.ProductOrderRepository;
import com.ringme.cms.repository.gym.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Log4j2
@RequiredArgsConstructor
public class ProductOrderService {

    private final ProductOrderRepository productOrderRepository;

    private final ProductRepository productRepository;

    private final RabbitTemplate rabbitTemplate;
    private final ProductOrderItemRepository productOrderItemRepository;

    @Value("${queue.order.checkout}")
    private String queueCheckout;

    public Long save(List<ProductDto> dtos, String type)
    {
        try{
            ProductOrder productOrder = new ProductOrder();
            productOrder.setOrderTime(LocalDateTime.now());
            productOrder.setStatus(2);
            ProductOrder savedData =  productOrderRepository.save(productOrder);
            Map<String,Object> map = new HashMap<>();
            map.put("products",dtos);
            map.put("type", type);
            map.put("orderId", savedData.getId());
                rabbitTemplate.convertAndSend(queueCheckout, map);
            return savedData.getId();
        }
        catch(Exception ex)
        {
            log.error(ex);
            return null;
        }
    }

    @Transactional
    public PreviousOrderDto getPreviousOrder () throws  Exception
    {
        PreviousOrderDto previousOrderDto = new PreviousOrderDto();
        List<ProductOrderItem> productsOrderItems = productOrderItemRepository.findByPreviousOrder();
        List<ProductDto> products = new ArrayList<>();
        BigDecimal totalTax = new BigDecimal(0);
        BigDecimal totalPrice = new BigDecimal(0);
        for (ProductOrderItem item : productsOrderItems) {
            Product product = item.getProduct();
            ProductDto productDto = new ProductDto();
            productDto.setId(product.getId());
            productDto.setName(product.getName());
            productDto.setPrice(product.getPrice());
            productDto.setAmount(item.getQuantity());
            products.add(productDto);
            BigDecimal price = product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            BigDecimal taxRate = product.getProductCategory().getTaxRate();
            BigDecimal fraction = taxRate.divide(BigDecimal.valueOf(100));
            BigDecimal result = fraction.multiply(price);
            BigDecimal formatted = result.setScale(2, RoundingMode.HALF_UP);
            totalTax = totalTax.add(formatted);
            totalPrice = totalPrice.add(price).setScale(2, RoundingMode.HALF_UP);
        }
        previousOrderDto.setProducts(products);
        previousOrderDto.setTax(totalTax);
        previousOrderDto.setTotal(totalPrice.add(totalTax));
        return previousOrderDto;
    }
}
