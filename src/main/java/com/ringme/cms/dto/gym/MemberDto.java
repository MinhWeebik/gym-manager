package com.ringme.cms.dto.gym;

import com.ringme.cms.model.gym.MemberSubscription;
import com.ringme.cms.validationfield.CheckFile;
import com.ringme.cms.validationfield.DatePattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@CheckFile(fileField = "imageUpload", mimeTypes = {"image/jpeg", "image/png"}, maxFileSize = 5 * 1024 * 1024,
        messageFileSize = "The uploaded file must be smaller than 5MB.",
        messageMimeTypes = "Only .jpg, .png, .jpeg files are allowed to be uploaded."
)
public class MemberDto {
    private Long id;
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    private Integer status;
    @DatePattern(pattern = "dd/MM/yyyy")
    private String dateOfBirthString;
    @NotNull
    private Integer gender;
    private String email;
    @NotBlank
    private String phoneNumber;
    private String imageUpload;
    private String address;
    private String homeNumber;
    private String workNumber;
    private String city;
    private String district;
    private String note;
    private List<MemberSubscription> normalSubscription = new ArrayList<>();
    private List<MemberSubscription> addonSubscription = new ArrayList<>();
    private List<MemberSubscription> oldSubscription = new ArrayList<>();

}