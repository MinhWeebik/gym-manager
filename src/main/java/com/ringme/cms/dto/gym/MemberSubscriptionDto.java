package com.ringme.cms.dto.gym;

import com.ringme.cms.validationfield.DatePattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberSubscriptionDto {
    private Long id;
    @NotBlank
    private String startEndString;
    @NotNull
    private Integer status;
    private Integer numberOfVisit;
    private Integer isRecurring;
    private String paypalSubscriptionId;
}
