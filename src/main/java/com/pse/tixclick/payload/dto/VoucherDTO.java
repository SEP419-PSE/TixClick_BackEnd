package com.pse.tixclick.payload.dto;


import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VoucherDTO {
     int voucherId;

     String voucherName;

     String voucherCode;

     double discount;

     String status;

     LocalDateTime createdDate;

     int accountId;
}
