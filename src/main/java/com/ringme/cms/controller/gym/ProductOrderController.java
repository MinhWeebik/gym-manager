package com.ringme.cms.controller.gym;

import com.ringme.cms.dto.gym.PaymentDto;
import com.ringme.cms.dto.gym.ProductDto;
import com.ringme.cms.dto.gym.SubscriptionDonutGraphData;
import com.ringme.cms.service.gym.ProductOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Log4j2
@RequestMapping("/product-order")
@RequiredArgsConstructor
public class ProductOrderController {

    private final ProductOrderService productOrderService;

    @PostMapping("/save")
    public ResponseEntity<Map<String, Object>> savePosCheckout(@Valid @RequestBody List<ProductDto> cartProduct,
                                                               @RequestParam(name = "type") String type) {
        try
        {
            Map<String, Object> map = new HashMap<>();
            map.put("code", 200);
            Long id = productOrderService.save(cartProduct, type);
            if(id != null)
            {
                map.put("data", id);
            }
            map.put("message", "success");
            return new ResponseEntity<>(map, HttpStatus.CREATED);
        }
        catch (Exception e)
        {
            log.error("Error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/get-previous-order")
    public ResponseEntity<Map<String,Object>> getPreviousOrder(){
        try {
            Map<String,Object> map = new HashMap<>();
            map.put("code", 200);
            map.put("data", productOrderService.getPreviousOrder());
            map.put("message", "success");
            return ResponseEntity.ok(map);
        }
        catch (Exception e)
        {
            log.error("Error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/donut-graph-data")
    @ResponseBody
    public ResponseEntity<List<SubscriptionDonutGraphData>> graphData(@RequestParam(name = "range") String range)
    {
        try
        {
            List<SubscriptionDonutGraphData> data = productOrderService.getDonutGraphData(range);
            return ResponseEntity.ok(data);
        }
        catch (Exception e)
        {
            log.error("Error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
