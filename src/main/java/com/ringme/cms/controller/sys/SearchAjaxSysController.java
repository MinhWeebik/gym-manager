package com.ringme.cms.controller.sys;

import com.ringme.cms.common.Helper;
import com.ringme.cms.dto.AjaxSearchDto;
import com.ringme.cms.model.sys.Icon;
import com.ringme.cms.model.sys.Router;
import com.ringme.cms.repository.sys.IconRepository;
import com.ringme.cms.service.sys.IconService;
import com.ringme.cms.service.sys.MenuService;
import com.ringme.cms.service.sys.RouterService;
import com.ringme.cms.service.sys.UserService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@Log4j2
@RequestMapping("/sys")
public class SearchAjaxSysController {
    @Autowired
    MenuService menuService;
    @Autowired
    RouterService routerService;
    @Autowired
    IconRepository iconRepository;
    @Autowired
    UserService userService;

    @GetMapping("/menu/ajax-search")
    public ResponseEntity<List<AjaxSearchDto>> menuAjaxSearch(@RequestParam(name = "input", required = false) String input) {
        return new ResponseEntity<>(menuService.ajaxSearch(input), HttpStatus.OK);
    }

    @GetMapping("/menu/ajax-search-2")
    public ResponseEntity<List<Map<String, String>>> menuAjaxSearch2(@RequestParam(name = "input", required = false) String input) {
        return new ResponseEntity<>(menuService.ajaxSearch2(input), HttpStatus.OK);
    }

    @GetMapping("/router/ajax-search")
    public ResponseEntity<List<Map<String, String>>> routerAjaxSearch(@RequestParam(name = "input", required = false) String input) {
        return new ResponseEntity<>(routerService.ajaxSearch(input), HttpStatus.OK);
    }

    @GetMapping("/icon/ajax-search")
    public ResponseEntity<List<Map<String, String>>> iconAjaxSearch(@RequestParam(name = "input", required = false) String input) {
        List<Icon> list = iconRepository.ajaxSearch(Helper.processStringSearch(input));

        List<Map<String, String>> result = list.stream().map(item -> {
            Map<String, String> map = new HashMap<>();
            map.put("id", item.getId().toString());
            map.put("text", item.getDisplayName() + " : " + item.getName());
            return map;
        }).collect(Collectors.toList());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/user/ajax-search")
    public ResponseEntity<List<AjaxSearchDto>> userAjaxSearch(@RequestParam(name = "input", required = false) String input) {
        return new ResponseEntity<>(userService.ajaxSearchCreated(input), HttpStatus.OK);
    }
}
