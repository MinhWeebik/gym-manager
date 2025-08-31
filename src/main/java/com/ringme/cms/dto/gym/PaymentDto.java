package com.ringme.cms.dto.gym;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentDto {
    private Long id;
    private Long memberId;
    @NotBlank
    private String description;
    @NotNull
    private Long amount;
    @NotBlank
    private String paymentGateway;
    @NotNull
    private Integer type;
    private String gatewayTransactionId;
    private String paymentUrl;
    private Integer tab;
    private Boolean shouldReload;
}
