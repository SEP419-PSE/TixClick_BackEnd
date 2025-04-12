package com.pse.tixclick.payload.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CompanyDocumentDTO {
    int companyDocumentId;
    int companyId;
    String fileName;
    String fileURL;
    String fileType;
    LocalDateTime uploadDate;
}
