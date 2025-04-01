package com.pse.tixclick.payload.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ContractDTO {
    int contractId;
    String contractName;
    double totalAmount;
    String commission;
    String contractType;
    LocalDate startDate;
    LocalDate endDate;
    String status;
    int accountId;
    int eventId;
    int companyId;
}
