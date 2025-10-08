package com.ringme.cms.service.gym;

import com.ringme.cms.dto.gym.ClassDto;
import com.ringme.cms.dto.gym.ProductDto;
import com.ringme.cms.dto.gym.ProductFormDto;
import com.ringme.cms.model.gym.Attendance;
import com.ringme.cms.model.gym.Classes;
import com.ringme.cms.model.gym.Product;
import com.ringme.cms.repository.gym.ProductCategoryRepository;
import com.ringme.cms.repository.gym.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Log4j2
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;

    private final ModelMapper modelMapper;

    private final ProductCategoryRepository productCategoryRepository;

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
            BigDecimal price = product.getPrice().multiply(BigDecimal.valueOf(item.getAmount()));
            BigDecimal taxRate = product.getProductCategory().getTaxRate();
            BigDecimal fraction = taxRate.divide(BigDecimal.valueOf(100));
            BigDecimal result = fraction.multiply(price);
            BigDecimal formatted = result.setScale(2, RoundingMode.HALF_UP);
            totalTax = totalTax.add(formatted);
        }
        return totalTax;
    }

    public Page<Product> getPage(String name, Integer status, Long category, Integer pageNo, Integer pageSize)
    {
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize);
        return productRepository.getPage(name, status, category ,pageable);
    }

    public void save(ProductFormDto formDto) throws Exception {
        try {
            Product product;
            if (formDto.getId() == null) {
                product = modelMapper.map(formDto, Product.class);
                product.setCreatedAt(LocalDateTime.now());
                product.setUpdatedAt(LocalDateTime.now());
                product.setStatus(1);
            } else {
                product = productRepository.findById(formDto.getId()).orElseThrow();
                modelMapper.map(formDto, product);
                product.setUpdatedAt(LocalDateTime.now());
            }
            if(formDto.getCategoryId() != null)
            {
                product.setProductCategory(productCategoryRepository.findById(formDto.getCategoryId()).orElseThrow());
            }
            productRepository.save(product);
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public void softDelete(Long id) throws Exception{
        Product product = productRepository.findById(id).orElseThrow(() -> new Exception("Không tìm thấy sản phẩm"));
        product.setStatus(0);
        productRepository.save(product);
    }

    public Page<Product> search(String name, Integer status, List<Long> exceptIds, Integer pageNo, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize);
        if (exceptIds == null || exceptIds.isEmpty()) {
            return productRepository.getPage(name,status, null,pageable);
        }
        return productRepository.search(name,status,exceptIds,pageable);
    }

    public void addContent(Long categoryId, Long productId) throws Exception
    {
        Product product = productRepository.findById(productId).orElseThrow();
        product.setProductCategory(productCategoryRepository.findById(categoryId).orElseThrow());
        productRepository.save(product);
    }

    public void removeContent(Long categoryId, Long productId) throws Exception
    {
        Product product = productRepository.findById(productId).orElseThrow();
        product.setProductCategory(null);
        productRepository.save(product);
    }
}
