package com.pse.tixclick.payload.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QRCompanyResponse {
    String bankID;
    String accountID;
    double amount;

    String description;
}
