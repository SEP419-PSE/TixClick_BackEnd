package com.pse.tixclick.payload.request.create;

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
    String fileName;
    String fileURL;
    String fileType;
    LocalDateTime uploadDate;
}
