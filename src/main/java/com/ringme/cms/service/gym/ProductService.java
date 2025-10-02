package com.ringme.cms.service.gym;

import com.ringme.cms.dto.gym.ProductDto;
import com.ringme.cms.model.gym.Product;
import com.ringme.cms.repository.gym.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Log4j2
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;

    public Map<String, Object> getFromCategory(Long id) throws  Exception{
        Map<String,Object> map = new HashMap<>();
        map.put("code", 200);
        map.put("data", productRepository.findByCategoryId(id));
        map.put("message","success");
        return map;
    }

    public BigDecimal calculateTax(List<ProductDto> dto) throws Exception
    {
        BigDecimal totalTax = new BigDecimal(0);
        for(ProductDto item : dto)
        {
            Product product = productRepository.findById(item.getId()).orElseThrow();
            int price = product.getPrice() * item.getAmount();
            BigDecimal taxRate = product.getProductCategory().getTaxRate();
            BigDecimal fraction = taxRate.divide(BigDecimal.valueOf(100));
            BigDecimal result = fraction.multiply(BigDecimal.valueOf(price));
            BigDecimal formatted = result.setScale(2, RoundingMode.HALF_UP);
            totalTax = totalTax.add(formatted);
        }
        return totalTax;
    }
}
