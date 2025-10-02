package com.ringme.cms.service.gym;

import com.ringme.cms.model.gym.ProductCategory;
import com.ringme.cms.repository.gym.ProductCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Log4j2
@RequiredArgsConstructor
public class ProductCategoryService {

    private final ProductCategoryRepository productCategoryRepository;

    public List<ProductCategory> getAll() {
        return productCategoryRepository.getAll();
    }

}
