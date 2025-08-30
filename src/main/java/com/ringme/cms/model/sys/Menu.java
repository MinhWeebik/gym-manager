package com.ringme.cms.model.sys;

import com.ringme.cms.dto.sys.MenuDto;
import lombok.*;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@ToString
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "tbl_menu")
public class Menu extends EntityBase implements Serializable {
    private static final long serialVersionUID = -297553281792804396L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name_vn")
    private String nameVn;
    @Column(name = "name_en")
    private String nameEn;
    @Column(name = "order_num")
    private Integer orderNum;
    @Column(name = "icon_id")
    private Long iconId;
    @Column(name = "parent_name_id")
    private Long parentNameId;
    @Column(name = "router_id")
    private Long routerId;

    @OneToOne
    @JoinColumn(name = "router_id", insertable = false, updatable = false)
    private Router router;
    @ManyToOne
    @JoinColumn(name = "parent_name_id", insertable = false, updatable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    private Menu parentName;
    @ManyToOne
    @JoinColumn(name = "icon_id", insertable = false, updatable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    private Icon icon;

    public MenuDto convertToDto(String locate){
        MenuDto menuDto = new MenuDto();
        if (locate.equals("vn")){
            menuDto.setId(this.id);
            menuDto.setOrder_num(this.orderNum);
            menuDto.setRouter(this.router);
            menuDto.setName(this.nameVn);
            menuDto.setIcon(this.icon);
        }
        else {
            menuDto.setId(this.id);
            menuDto.setOrder_num(this.orderNum);
            menuDto.setRouter(this.router);
            menuDto.setName(this.nameEn);
            menuDto.setIcon(this.icon);
        }
        return menuDto;
    }
}
