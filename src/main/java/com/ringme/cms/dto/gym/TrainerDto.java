package com.ringme.cms.dto.gym;

import com.ringme.cms.validationfield.CheckFile;
import com.ringme.cms.validationfield.DatePattern;
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
@CheckFile(fileField = "imageUpload", mimeTypes = {"image/jpeg", "image/png"}, maxFileSize = 5 * 1024 * 1024,
        messageFileSize = "The uploaded file must be smaller than 5MB.",
        messageMimeTypes = "Only .jpg, .png, .jpeg files are allowed to be uploaded.", required = false
)
public class TrainerDto {
    private Long id;
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    private Integer status;
    @DatePattern(pattern = "dd/MM/yyyy")
    private String dateOfBirthString;
    private String hireDateString;
    @NotNull
    private Integer gender;
    private String email;
    @NotBlank
    private String phoneNumber;
    private String imageUpload;
    private String address;
    private String city;
    private String district;
    private String bio;
}
