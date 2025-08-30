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
@Table(name = "tbl_user_role")
public class UserRole extends EntityBase implements Serializable {
    private static final long serialVersionUID = -297553281792804396L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "role_id")
    private Long roleId;
    @Column(name = "user_id")
    private Long userId;

    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    private User user;
    @ManyToOne
    @JoinColumn(name = "role_id", insertable = false, updatable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    private Role role;
    public UserRole(User user, Role role) {
        this.user = user;
        this.role = role;
    }
}
