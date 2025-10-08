package com.ringme.cms.model.gym;

import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "product_order")
public class ProductOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "status") //0 inactive 1 active 2 pending
    private Integer status;

    @Column(name = "order_time")
    private LocalDateTime orderTime;

    @OneToOne(
            mappedBy = "productOrder"
    )
    private Payment payment;

    @OneToMany(mappedBy = "productOrder", fetch = FetchType.LAZY)
    private List<ProductOrderItem> productOrderItems;
}
