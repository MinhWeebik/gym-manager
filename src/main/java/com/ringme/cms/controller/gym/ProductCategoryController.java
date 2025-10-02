package com.ringme.cms.controller.gym;

import com.ringme.cms.model.gym.ProductCategory;
import com.ringme.cms.service.gym.ProductCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Log4j2
@RequestMapping("/product-category")
@RequiredArgsConstructor
public class ProductCategoryController {

    private final ProductCategoryService productCategoryService;

    @GetMapping("/get-all")
    public ResponseEntity<Map<String, Object>> getAll()
    {
        try
        {
            List<ProductCategory> productCategories = productCategoryService.getAll();
            Map<String, Object> map = new HashMap<>();
            map.put("code", 200);
            map.put("data", productCategories);
            map.put("message", "success");
            return new ResponseEntity<>(map, HttpStatus.OK);
        }
        catch (Exception ex)
        {
            return ResponseEntity.badRequest().build();
        }
    }
}
