package com.pse.tixclick.payload.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ContractDocumentDTO {
    int contractDocumentId;
    int contractId;
    String fileName;
    String fileURL;
    String fileType;
    int uploadedBy;
    String status;
    LocalDateTime uploadDate;
}
