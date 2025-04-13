package com.pse.tixclick.payload.response;

import com.pse.tixclick.payload.entity.entity_enum.EContractDetailStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QRCompanyResponse {
    String bankID;
    String accountID;
    double amount;
    LocalDate dueDate;
    String description;
    EContractDetailStatus status;
}
