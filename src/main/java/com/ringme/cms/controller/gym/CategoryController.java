package com.ringme.cms.controller.gym;

import com.ringme.cms.dto.AjaxSearchDto;
import com.ringme.cms.dto.gym.CategoryDto;
import com.ringme.cms.dto.gym.ProductFormDto;
import com.ringme.cms.model.gym.Member;
import com.ringme.cms.model.gym.Product;
import com.ringme.cms.model.gym.ProductCategory;
import com.ringme.cms.model.gym.ScheduledClass;
import com.ringme.cms.repository.gym.ProductCategoryRepository;
import com.ringme.cms.repository.gym.ProductRepository;
import com.ringme.cms.service.gym.CategoryService;
import com.ringme.cms.service.gym.ProductService;
import com.ringme.cms.utils.AppUtils;
import jdk.jfr.Category;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

@Controller
@Log4j2
@RequestMapping("/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    private final ProductCategoryRepository  productCategoryRepository;

    private final ModelMapper modelMapper;

    private final ProductRepository productRepository;
    private final ProductService productService;

    @RequestMapping(value = {"/index"})
    public String index(@RequestParam(name = "pageNo", required = false, defaultValue = "1") Integer pageNo,
                        @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                        @RequestParam(name = "name", required = false) String name,
                        @RequestParam(name = "status", required = false) Integer status,
                        ModelMap model) {
        if(pageNo == null || pageNo <= 0) pageNo = 1;
        if(pageSize == null || pageSize <= 0) pageSize = 15;
        Page<ProductCategory> pageObject = categoryService.getPage(name, status, pageNo, pageSize);
        model.put("title", "Danh mục");
        model.put("pageNo", pageNo);
        model.put("pageSize", pageSize);
        model.put("totalPage", pageObject.getTotalPages());
        model.put("models", pageObject.getContent());
        model.put("name", name);
        model.put("status", status);
        return "gym/category/index";
    }

    @GetMapping("/form")
    public String form(@RequestParam(value = "id", required = false) Long id, ModelMap model) {
        log.info("id: {}", id);
        try {
            model.put("title", id != null ? "Cập nhật danh mục" : "Thêm danh mục");
            CategoryDto formDto = (CategoryDto) model.getOrDefault("form", new CategoryDto());
            if (id != null) {
                ProductCategory category = productCategoryRepository.findById(id).orElseThrow();
                if (!model.containsAttribute("form")) {
                    formDto = modelMapper.map(category, CategoryDto.class);
                }
            }

            model.putIfAbsent("form", formDto);

            return "gym/category/form";
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            return "404";
        }
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("form") CategoryDto formDto,
                       BindingResult bindingResult,
                       RedirectAttributes redirectAttributes,
                       HttpServletRequest request) {
        try {
            if (bindingResult.hasErrors()) {
                return AppUtils.goBackWithError(request, redirectAttributes, "form", bindingResult, formDto, null)
                        .orElse("redirect:/category/form?id=" + formDto.getId());
            }
            categoryService.save(formDto);
            if (formDto.getId() == null) {
                redirectAttributes.addFlashAttribute("success", "Tạo thành công!");
                return "redirect:/category/index";
            } else {
                redirectAttributes.addFlashAttribute("success", "Cập nhật thành công!");
            }
            return AppUtils.goBack(request).orElse("redirect:/category/form?id=" + formDto.getId());
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            return AppUtils.goBackWithError(request, redirectAttributes, "form", bindingResult, formDto, "Error in server!")
                    .orElse("redirect:/category/form?id=" + formDto.getId());
        }
    }

    @GetMapping("/ajax-search")
    @ResponseBody
    public ResponseEntity<List<AjaxSearchDto>> classAjaxSearch(
            @RequestParam(name = "input", required = false) String input) {
        try
        {
            return new ResponseEntity<>(categoryService.ajaxSearchCategory(input), HttpStatus.OK);
        }
        catch (Exception e)
        {
            log.error("Error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/get-content-ids")
    @ResponseBody
    public ResponseEntity<?> getContentIds() {
        try {
            Set<Long> contentIds = productRepository.findIdByCategoryId();
            return ResponseEntity.ok(contentIds);
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error");
        }
    }

    @RequestMapping(value = {"/product"})
    @Transactional
    public String index(@RequestParam(name = "id") Long id,
                        ModelMap model) {
        log.info("id: {}", id);
        try {
            ProductCategory productCategory = productCategoryRepository.findById(id).orElseThrow();
            List<Product> products = productRepository.findByCategoryId(id);
            model.put("title", "Sản phẩm của " + productCategory.getName());
            model.put("models", products);
            model.put("id" ,id);
            return "gym/category/product";
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            return "404";
        }
    }

    @PostMapping("/add-content")
    @ResponseBody
    public ResponseEntity<?> addContent(@RequestParam(name = "categoryId") Long categoryId,
                                        @RequestParam("productId") Long productId) {
        try {
            productService.addContent(categoryId, productId);
            return ResponseEntity.ok("success");
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("error");
        }
    }

    @PostMapping("/remove-content")
    @ResponseBody
    public ResponseEntity<?> removeContent(@RequestParam(name = "categoryId") Long categoryId,
                                           @RequestParam("productId") Long productId) {
        try {
            productService.removeContent(categoryId, productId);
            return ResponseEntity.ok("success");
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("error");
        }
    }

    @PostMapping(value = {"/delete/{id}"})
    public String delete(@PathVariable(required = true) Long id,
                         HttpServletRequest request,
                         RedirectAttributes redirectAttributes) {
        log.info("id: {}", id);
        try {
            categoryService.softDelete(id);
            redirectAttributes.addFlashAttribute("success", "Xóa thành công!");
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return AppUtils.goBack(request).orElse("redirect:/category/index");
    }
}
