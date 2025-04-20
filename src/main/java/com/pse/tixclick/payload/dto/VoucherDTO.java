package com.pse.tixclick.payload.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
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
     @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
     private LocalDateTime startDate;

     @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
     private LocalDateTime endDate;

     double quantity;

     double discount;

     String status;

     LocalDateTime createdDate;

     int accountId;
}
