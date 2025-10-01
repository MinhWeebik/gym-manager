package com.ringme.cms.controller.gym;

import com.ringme.cms.dto.gym.ClassDto;
import com.ringme.cms.dto.gym.CoinDiscountTierDto;
import com.ringme.cms.model.gym.Classes;
import com.ringme.cms.model.gym.CoinDiscountTier;
import com.ringme.cms.model.gym.GeneralSettings;
import com.ringme.cms.repository.gym.CoinDiscountTierRepository;
import com.ringme.cms.repository.gym.GeneralSettingsRepository;
import com.ringme.cms.service.gym.CoinService;
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
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Controller
@Log4j2
@RequestMapping("/coin")
@RequiredArgsConstructor
public class CoinController {

    private final CoinService coinService;

    private final GeneralSettingsRepository generalSettingsRepository;

    private final CoinDiscountTierRepository coinDiscountTierRepository;

    private final ModelMapper modelMapper;

    @RequestMapping(value = {"/index"})
    public String index(@RequestParam(name = "pageNo", required = false, defaultValue = "1") Integer pageNo,
                        @RequestParam(name = "pageSize", required = false, defaultValue = "15") Integer pageSize,
                        ModelMap model) {
        if(pageNo == null || pageNo <= 0) pageNo = 1;
        if(pageSize == null || pageSize <= 0) pageSize = 15;
        Page<CoinDiscountTier> pageObject = coinService.getPage(pageNo, pageSize);
        GeneralSettings generalSettings = generalSettingsRepository.findBySettingKey("coin_price");
        int coinPrice = Integer.parseInt(generalSettings.getSettingValue());
        model.put("title", "Xu");
        model.put("pageNo", pageNo);
        model.put("pageSize", pageSize);
        model.put("totalPage", pageObject.getTotalPages());
        model.put("models", pageObject.getContent());
        model.put("coinPrice", coinPrice);
        return "gym/coin/index";
    }

    @GetMapping("/check-coin-price")
    @ResponseBody
    public ResponseEntity<Map<String,Object>> checkCoinPrice(@RequestParam("amount") Integer amount)
    {
        try
        {
            Map<String, Object> map = new HashMap<>();
            GeneralSettings gs = generalSettingsRepository.findBySettingKey("coin_price");
            Integer price = Integer.parseInt(gs.getSettingValue());
            Integer totalPrice = price * amount;
            Integer bonusCoin = 0;
            map.put("price", totalPrice);
            List<CoinDiscountTier> CDTList = coinDiscountTierRepository.getAll();
            for (CoinDiscountTier CDT : CDTList)
            {
                if(amount >= CDT.getMinCoins())
                {
                    BigDecimal fraction = CDT.getBonusPct().divide(BigDecimal.valueOf(100));
                    BigDecimal result = fraction.multiply(BigDecimal.valueOf(amount));
                    bonusCoin = result.setScale(2, RoundingMode.HALF_UP).intValue();
                    break;
                }
            }
            map.put("bonusCoin", bonusCoin);
            return ResponseEntity.ok().body(map);
        }
        catch (Exception e)
        {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/form")
    public String form(@RequestParam(value = "id", required = false) Long id, ModelMap model) {
        log.info("id: {}", id);
        try {
            model.put("title", id != null ? "Cập nhật giảm giá xu" : "Thêm giảm giá xu");
            CoinDiscountTierDto formDto = (CoinDiscountTierDto) model.getOrDefault("form", new CoinDiscountTierDto());
            if (id != null) {
                CoinDiscountTier CDT = coinDiscountTierRepository.findById(id).orElseThrow();
                if (!model.containsAttribute("form")) {
                    formDto = modelMapper.map(CDT, CoinDiscountTierDto.class);
                }
            }
            model.putIfAbsent("form", formDto);
            return "gym/coin/form";
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            return "404";
        }
    }

    @PostMapping("/save-coin")
    public String save(@Valid @RequestParam("coinPrice") Integer coinPrice,
                       RedirectAttributes redirectAttributes,
                       HttpServletRequest request) {
        try {
            if(coinPrice == null || coinPrice <= 0)
            {
                redirectAttributes.addFlashAttribute("error", "Số tiền không hợp lệ");
                return AppUtils.goBack(request).orElse("redirect:/coin/index");
            }
            coinService.saveCoin(coinPrice);
            redirectAttributes.addFlashAttribute("success", "Cập nhật thành công!");
            return "redirect:/coin/index";
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Cập nhật thất bại");
            return AppUtils.goBack(request).orElse("redirect:/coin/index");
        }
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("form") CoinDiscountTierDto formDto,
                       BindingResult bindingResult,
                       RedirectAttributes redirectAttributes,
                       HttpServletRequest request) {
        try {
            if (bindingResult.hasErrors()) {
                return AppUtils.goBackWithError(request, redirectAttributes, "form", bindingResult, formDto, null)
                        .orElse("redirect:/coin/form?id=" + formDto.getId());
            }
            coinService.save(formDto);
            if (formDto.getId() == null) {
                redirectAttributes.addFlashAttribute("success", "Tạo thành công!");
                return "redirect:/coin/index";
            } else {
                redirectAttributes.addFlashAttribute("success", "Cập nhật thành công!");
            }
            return AppUtils.goBack(request).orElse("redirect:/coin/form?id=" + formDto.getId());
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            return AppUtils.goBackWithError(request, redirectAttributes, "form", bindingResult, formDto, "Error in server!")
                    .orElse("redirect:/coin/form?id=" + formDto.getId());
        }
    }

    @PostMapping(value = {"/delete/{id}"})
    public String delete(@PathVariable(required = true) Long id,
                         HttpServletRequest request,
                         RedirectAttributes redirectAttributes) {
        log.info("id: {}", id);
        try {
            coinService.softDelete(id);
            redirectAttributes.addFlashAttribute("success", "Xóa thành công!");
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return AppUtils.goBack(request).orElse("redirect:/coin/index");
    }
}
