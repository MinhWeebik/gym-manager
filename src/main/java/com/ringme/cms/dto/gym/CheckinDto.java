package com.ringme.cms.dto.gym;

import com.ringme.cms.model.gym.MemberSubscription;
import lombok.*;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CheckinDto {
    private String firstName;
    private String lastName;
    private String imageUrl;
    private String membershipName;
    private String startAt;
    private String endAt;
}
