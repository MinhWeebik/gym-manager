package com.ringme.cms.controller.gym;

import com.ringme.cms.model.gym.Classes;
import com.ringme.cms.model.gym.ProductCategory;
import com.ringme.cms.service.gym.ProductCategoryService;
import com.ringme.cms.service.gym.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@Log4j2
@RequestMapping("/pos")
@RequiredArgsConstructor
public class PointOfSaleController {

    private final ProductService productService;

    private final ProductCategoryService productCategoryService;

    @RequestMapping(value = {"/index"})
    public String index(ModelMap model) {
        List<ProductCategory> productCategoryServiceList = productCategoryService.getAll();
        model.put("title", "Bán hàng");
        model.put("models", productCategoryServiceList);
        return "gym/pos/index";
    }
}
