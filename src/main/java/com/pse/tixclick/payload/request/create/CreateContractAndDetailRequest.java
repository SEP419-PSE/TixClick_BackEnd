package com.pse.tixclick.payload.request.create;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateContractAndDetailRequest {
    String contractName;
    String contractCode;
    LocalDate contractStartDate;
    LocalDate contractEndDate;
    String representativeA;
    String emailA;
    String representativeB;
    String emailB;
    Double contractValue;
    String commission;
    String contractType;
    String eventCode;
    List<ContractDetailRequest> contractDetails;
}
