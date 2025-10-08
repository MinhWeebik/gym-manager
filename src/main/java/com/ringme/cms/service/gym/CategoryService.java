package com.ringme.cms.service.gym;

import com.ringme.cms.common.Helper;
import com.ringme.cms.dto.AjaxSearchDto;
import com.ringme.cms.dto.gym.CategoryDto;
import com.ringme.cms.dto.gym.ProductFormDto;
import com.ringme.cms.model.gym.Product;
import com.ringme.cms.model.gym.ProductCategory;
import com.ringme.cms.repository.gym.ProductCategoryRepository;
import com.ringme.cms.repository.gym.ProductRepository;
import jdk.jfr.Category;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Log4j2
@RequiredArgsConstructor
public class CategoryService {

    private final ProductCategoryRepository productCategoryRepository;

    private final ModelMapper modelMapper;

    private final ProductRepository productRepository;

    public List<AjaxSearchDto> ajaxSearchCategory(String input) {
        return Helper.listAjax(productCategoryRepository.ajaxSearchCategory(Helper.processStringSearch(input)),1);
    }

    public Page<ProductCategory> getPage(String name, Integer status, Integer pageNo, Integer pageSize)
    {
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize);
        return productCategoryRepository.getPage(name, status ,pageable);
    }

    public void save(CategoryDto formDto) throws Exception {
        try {
            ProductCategory category;
            if (formDto.getId() == null) {
                category = modelMapper.map(formDto, ProductCategory.class);
                category.setCreatedAt(LocalDateTime.now());
                category.setUpdatedAt(LocalDateTime.now());
                category.setStatus(1);
            } else {
                category = productCategoryRepository.findById(formDto.getId()).orElseThrow();
                modelMapper.map(formDto, category);
                category.setUpdatedAt(LocalDateTime.now());
            }
            productCategoryRepository.save(category);
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public void softDelete(Long id) throws Exception{
        ProductCategory category = productCategoryRepository.findById(id).orElseThrow(() -> new Exception("Không tìm thấy danh mục"));
        category.setStatus(0);
        productCategoryRepository.save(category);
        List<Product> products = productRepository.findByCategoryId(id);
        for (Product product : products) {
            product.setProductCategory(null);
            productRepository.save(product);
        }
    }
}
