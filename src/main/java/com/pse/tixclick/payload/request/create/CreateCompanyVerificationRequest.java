package com.pse.tixclick.payload.request.create;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pse.tixclick.payload.entity.entity_enum.EVerificationStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateCompanyVerificationRequest {
    int companyId;
    int submitById;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime submitDate;

    EVerificationStatus status;
}
