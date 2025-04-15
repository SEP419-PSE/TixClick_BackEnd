package com.pse.tixclick.payload.response;

import com.pse.tixclick.payload.dto.ContractDTO;
import com.pse.tixclick.payload.dto.ContractDetailDTO;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GetTransactionPaymenByCompanyIdResponse {
    List<ContractResponse> contractDTOS;
}
