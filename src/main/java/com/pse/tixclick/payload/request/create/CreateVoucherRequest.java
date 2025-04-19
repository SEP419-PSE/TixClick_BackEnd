package com.pse.tixclick.payload.request.create;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateVoucherRequest {
    String voucherName;

    String voucherCode;

    double discount;

    int eventId;

    double quantity;

    LocalDateTime startDate;

    LocalDateTime endDate;
}
