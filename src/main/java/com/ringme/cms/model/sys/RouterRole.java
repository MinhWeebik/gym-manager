package com.ringme.cms.model.sys;

import lombok.*;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@ToString
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "tbl_router_role")
public class RouterRole extends EntityBase implements Serializable {
    private static final long serialVersionUID = -297553281792804396L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "role_id")
    private Long roleId;
    @Column(name = "router_id")
    private Long routerId;

    @ManyToOne
    @JoinColumn(name = "router_id", insertable = false, updatable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    private Router router;
    @ManyToOne
    @JoinColumn(name = "role_id", insertable = false, updatable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    private Role role;
}
