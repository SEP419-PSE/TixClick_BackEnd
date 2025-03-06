package com.pse.tixclick.payload.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CompanyVerificationDTO {
    int companyVerificationId;
    int companyId;
    int submitById;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime submitDate;

    String note;
    String status;
}
