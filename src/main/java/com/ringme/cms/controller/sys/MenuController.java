package com.ringme.cms.controller.sys;

import com.ringme.cms.dto.sys.MenuFormDto;
import com.ringme.cms.dto.sys.RouterFormDto;
import com.ringme.cms.model.sys.Icon;
import com.ringme.cms.model.sys.Menu;
import com.ringme.cms.model.sys.Router;
import com.ringme.cms.repository.sys.IconRepository;
import com.ringme.cms.repository.sys.MenuRepository;
import com.ringme.cms.service.sys.MenuService;
import com.ringme.cms.service.sys.RouterService;
import com.ringme.cms.utils.AppUtils;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@Controller
@Log4j2
@RequestMapping("/sys/menu")
public class MenuController {
    @Autowired
    MenuService menuService;
    @Autowired
    MenuRepository menuRepository;
    @Autowired
    RouterService routerService;
    @Autowired
    IconRepository iconRepository;
    @Autowired
    private ModelMapper modelMapper;

    @GetMapping(value = {"/index"})
    public String index(@RequestParam(name = "pageNo", required = false, defaultValue = "1") Integer pageNo,
                                 @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                                 @RequestParam(name = "parentNameId", required = false) Long parentNameId,
                                 ModelMap model) {
        log.info("pageNo: {}| pageSize: {}", pageNo, pageSize);
        if (pageNo == null || pageNo <= 0) pageNo = 1;
        if (pageSize == null || pageSize <= 0) pageSize = 10;

        Page<Menu> models = menuService.getPage(parentNameId, pageNo, pageSize);
        model.put("title", "Menu list");
        model.put("pageNo", pageNo);
        model.put("pageSize", pageSize);
        model.put("totalPage", models.getTotalPages());
        model.put("models", models.toList());
        model.put("parentNameId", parentNameId);
        if (parentNameId != null) {
            menuService.findMenuById(parentNameId).ifPresent(item -> {
                model.put("parentNameMenu", item.getNameEn() + " : " + item.getRouter().getRouterLink());
            });
        }
        return "sys/menu";
    }

    @GetMapping("/form")
    public String form(@RequestParam(value = "id", required = false) Long id, ModelMap model) {
        log.info("id: {}", id);
        try {
            model.put("title", id != null ? "Menu update #" + id : "Menu create");

            MenuFormDto formDto;
            if (model.containsAttribute("form")) {
                formDto = (MenuFormDto) model.getAttribute("form");
            } else if (id != null && !model.containsAttribute("form")) {
                formDto = modelMapper.map(menuService.findMenuById(id).orElseThrow(), MenuFormDto.class);
            } else {
                formDto = new MenuFormDto();
            }
            model.putIfAbsent("form", formDto);

            if (formDto.getParentNameId() != null) {
                menuRepository.findById(formDto.getParentNameId()).ifPresent(menu ->
                        model.put("parentNameMenu", menu.getNameEn() + " : " + menu.getRouter().getRouterLink())
                );
            }
            if (formDto.getIconId() != null) {
                iconRepository.findById(formDto.getIconId()).ifPresent(icon ->
                        model.put("iconName", icon.getDisplayName() + " : " + icon.getName())
                );
            }
            if (formDto.getRouterId() != null) {
                routerService.findRouterById(formDto.getRouterId()).ifPresent(router ->
                        model.put("routerName", router.getDes() + " : " + router.getRouterLink())
                );
            }

            return "sys/menu-form";
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            return "404";
        }
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("form") MenuFormDto formDto,
                       BindingResult bindingResult,
                       RedirectAttributes redirectAttributes,
                       HttpServletRequest request) {
        try {
            log.info("formDto: {}", formDto);
            if (bindingResult.hasErrors()) {
                return AppUtils.goBackWithError(request, redirectAttributes, "form", bindingResult, formDto, null)
                        .orElse("redirect:/sys/menu/form?id=" + formDto.getId());
            }
            menuService.save(formDto);
            if (formDto.getId() == null) {
                redirectAttributes.addFlashAttribute("success", "Create success!");
                return "redirect:/sys/menu/index";
            } else {
                redirectAttributes.addFlashAttribute("success", "Update success!");
            }
            return AppUtils.goBack(request).orElse("redirect:/sys/menu/form?id=" + formDto.getId());
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            return AppUtils.goBackWithError(request, redirectAttributes, "form", bindingResult, formDto, "Error in server!")
                    .orElse("redirect:/sys/menu/form?id=" + formDto.getId());
        }
    }

    @PostMapping(value = {"/delete/{id}"})
    public String delete(@PathVariable(required = true) Long id,
                         HttpServletRequest request,
                         RedirectAttributes redirectAttributes) {
        log.info("id: {}", id);
        try {
            menuService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Delete success!");
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error in server!");
        }
        return AppUtils.goBack(request).orElse("redirect:/sys/menu/index");
    }
}
