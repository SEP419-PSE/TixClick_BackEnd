package com.pse.tixclick.payload.request.create;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateContractRequest {
    int contractId;
    String contractName;
    double totalAmount;
    String commission;
    String contractType;
    int eventId;
}
