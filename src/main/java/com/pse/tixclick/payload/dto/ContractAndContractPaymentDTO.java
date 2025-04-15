package com.pse.tixclick.payload.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ContractAndContractPaymentDTO {
    ContractDTO contractDTO;
    List<ContractPaymentDTO> contractPaymentDTOList;
}
