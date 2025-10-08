package com.ringme.cms.controller.gym;

import com.ringme.cms.dto.gym.ClassDto;
import com.ringme.cms.dto.gym.ProductDto;
import com.ringme.cms.dto.gym.ProductFormDto;
import com.ringme.cms.model.gym.Classes;
import com.ringme.cms.model.gym.Product;
import com.ringme.cms.repository.gym.ProductCategoryRepository;
import com.ringme.cms.repository.gym.ProductRepository;
import com.ringme.cms.service.gym.ProductCategoryService;
import com.ringme.cms.service.gym.ProductService;
import com.ringme.cms.utils.AppUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@Log4j2
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController
{

    private final ProductService productService;
    private final ProductRepository  productRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ModelMapper modelMapper;

    @RequestMapping(value = {"/index"})
    public String index(@RequestParam(name = "pageNo", required = false, defaultValue = "1") Integer pageNo,
                        @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                        @RequestParam(name = "name", required = false) String name,
                        @RequestParam(name = "category", required = false) Long category,
                        @RequestParam(name = "status", required = false) Integer status,
                        ModelMap model) {
        if(pageNo == null || pageNo <= 0) pageNo = 1;
        if(pageSize == null || pageSize <= 0) pageSize = 10;
        Page<Product> pageObject = productService.getPage(name, status, category, pageNo, pageSize);
        model.put("title", "Sản phẩm");
        model.put("pageNo", pageNo);
        model.put("pageSize", pageSize);
        model.put("totalPage", pageObject.getTotalPages());
        model.put("models", pageObject.getContent());
        model.put("name", name);
        model.put("category", category);
        model.put("status", status);
        if (category != null) {
            productCategoryRepository.findById(category).ifPresent(item -> {
                model.put("categoryName", item.getName());
            });
        }
        return "gym/product/index";
    }

    @GetMapping("/form")
    public String form(@RequestParam(value = "id", required = false) Long id, ModelMap model) {
        log.info("id: {}", id);
        try {
            model.put("title", id != null ? "Cập nhật sản phẩm" : "Thêm sản phẩm");
            ProductFormDto formDto = (ProductFormDto) model.getOrDefault("form", new ProductFormDto());
            if (id != null) {
                Product product = productRepository.findById(id).orElseThrow();
                if (!model.containsAttribute("form")) {
                    formDto = modelMapper.map(product, ProductFormDto.class);
                    if(product.getProductCategory() != null)
                    {
                        formDto.setCategoryId(product.getProductCategory().getId());
                    }
                }
            }

            model.putIfAbsent("form", formDto);
            if (formDto.getCategoryId() != null) {
                productCategoryRepository.findById(Long.parseLong(formDto.getCategoryId().toString())).ifPresent(item -> {
                    model.put("categoryName", item.getName());
                });
            }

            return "gym/product/form";
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            return "404";
        }
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("form") ProductFormDto formDto,
                       BindingResult bindingResult,
                       RedirectAttributes redirectAttributes,
                       HttpServletRequest request) {
        try {
            if (bindingResult.hasErrors()) {
                return AppUtils.goBackWithError(request, redirectAttributes, "form", bindingResult, formDto, null)
                        .orElse("redirect:/product/form?id=" + formDto.getId());
            }
            productService.save(formDto);
            if (formDto.getId() == null) {
                redirectAttributes.addFlashAttribute("success", "Tạo thành công!");
                return "redirect:/product/index";
            } else {
                redirectAttributes.addFlashAttribute("success", "Cập nhật thành công!");
            }
            return AppUtils.goBack(request).orElse("redirect:/product/form?id=" + formDto.getId());
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            return AppUtils.goBackWithError(request, redirectAttributes, "form", bindingResult, formDto, "Error in server!")
                    .orElse("redirect:/product/form?id=" + formDto.getId());
        }
    }

    @PostMapping(value = {"/delete/{id}"})
    public String delete(@PathVariable(required = true) Long id,
                         HttpServletRequest request,
                         RedirectAttributes redirectAttributes) {
        log.info("id: {}", id);
        try {
            productService.softDelete(id);
            redirectAttributes.addFlashAttribute("success", "Xóa thành công!");
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return AppUtils.goBack(request).orElse("redirect:/product/index");
    }

    @GetMapping("/get-from-category")
    @ResponseBody
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
    @ResponseBody
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
    @ResponseBody
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

    @GetMapping(value = {"/render-search"})
    public String search(@RequestParam(name = "pageNo", required = false, defaultValue = "1") Integer pageNo,
                         @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                         @RequestParam(name = "name", required = false) String name,
                         @RequestParam(name = "status", required = false) Integer status,
                         @RequestParam(name = "exceptIds", required = false) List<Long> exceptIds,
                         ModelMap model) throws Exception {
        if (pageNo == null || pageNo <= 0) pageNo = 1;
        if (pageSize == null || pageSize <= 0) pageSize = 10;
        try {
            Page<Product> models = productService.search(name, status,exceptIds, pageNo, pageSize);
            model.put("pageNo", pageNo);
            model.put("pageSize", pageSize);
            model.put("totalPage", models.getTotalPages());
            model.put("models", models.toList());
            return "gym/category/modal_search :: content-search-song";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
