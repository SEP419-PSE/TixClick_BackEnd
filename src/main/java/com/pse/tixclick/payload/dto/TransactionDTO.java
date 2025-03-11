package com.pse.tixclick.payload.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pse.tixclick.payload.entity.Account;
import com.pse.tixclick.payload.entity.payment.ContractPayment;
import com.pse.tixclick.payload.entity.payment.Payment;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransactionDTO {
    private int transactionId;

    private double amount;

    private String description;

    private  String transactionCode;

    private String type;

    private LocalDateTime transactionDate;

    private int accountId;

    private int paymentId;

    private int contractPaymentId;
}
