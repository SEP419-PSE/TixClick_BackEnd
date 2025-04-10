package com.pse.tixclick.payload.dto;

import com.pse.tixclick.payload.entity.company.ContractDocument;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ContractAndDocumentsDTO {
    ContractDTO contractDTO;
    List<ContractDocumentDTO> contractDocumentDTOS;
    int contractVerificationId;
}
