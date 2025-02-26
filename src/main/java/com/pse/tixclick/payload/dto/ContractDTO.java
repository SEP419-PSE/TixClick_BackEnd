package com.pse.tixclick.payload.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ContractDTO {
    int contractId;
    double totalAmount;
    String commission;
    String contractType;
    int accountId;
    int eventId;
    int companyId;
}
