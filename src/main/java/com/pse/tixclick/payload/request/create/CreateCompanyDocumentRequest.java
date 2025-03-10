package com.pse.tixclick.payload.request.create;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateCompanyDocumentRequest {
    int companyId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    String uploadDate;

    int companyVerificationId;
}
