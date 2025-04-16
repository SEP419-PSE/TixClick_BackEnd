package com.pse.tixclick.payload.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransactionCompanyByEventDTO {
    private String transactionCode;
    private String accountName;
    private String accountMail;
    private String transactionType;
    private double amount;
    private LocalDateTime transactionDate;
    private String status;
}
