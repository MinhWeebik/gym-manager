package com.ringme.cms.dto.sys;

import com.ringme.cms.model.sys.Icon;
import com.ringme.cms.model.sys.Menu;
import com.ringme.cms.model.sys.Router;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
@AllArgsConstructor
public class MenuDto implements Serializable {

    private Long id;

    private String name;

    private Router router;

    private Integer order_num;

    private Menu parentName;

    private List<MenuDto> lstChildMenus;

    private Icon icon;

}
