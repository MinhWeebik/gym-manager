package com.ringme.cms.controller.gym;

import com.ringme.cms.dto.gym.ProductDto;
import com.ringme.cms.model.gym.Product;
import com.ringme.cms.repository.gym.ProductRepository;
import com.ringme.cms.service.gym.ProductCategoryService;
import com.ringme.cms.service.gym.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Log4j2
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController
{

    private final ProductService productService;
    private final ProductRepository  productRepository;

    @GetMapping("/get-from-category")
    public ResponseEntity<Map<String, Object>> getFromCategory(@RequestParam("id") Long id)
    {
        try
        {
            Map<String, Object> map = productService.getFromCategory(id);
            return new ResponseEntity<>(map, HttpStatus.OK);
        }
        catch (Exception ex)
        {
            log.error(ex.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/find-by-name")
    public ResponseEntity<Map<String, Object>> findByName(@RequestParam("name") String name)
    {
        try
        {
            List<Product> products = productRepository.findByName(name);
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("code", 200);
            map.put("data", products);
            map.put("message", "success");
            return new ResponseEntity<>(map, HttpStatus.OK);
        }
        catch (Exception ex)
        {
            log.error(ex.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/calculate-tax")
    public ResponseEntity<Map<String, Object>> calculateTax(@RequestBody List<ProductDto> dto)
    {
        try
        {
            BigDecimal taxAmount = productService.calculateTax(dto);
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("code", 200);
            map.put("data", taxAmount);
            map.put("message", "success");
            return new ResponseEntity<>(map, HttpStatus.OK);
        }
        catch (Exception ex)
        {
            log.error(ex.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
