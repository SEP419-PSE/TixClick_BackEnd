package com.pse.tixclick.payload.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pse.tixclick.payload.entity.Account;
import com.pse.tixclick.payload.entity.payment.Order;
import com.pse.tixclick.payload.entity.payment.Transaction;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.Collection;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentDTO {
    private int paymentId;

    private String paymentMethod;

    private double amount;

    private LocalDateTime paymentDate;

    private String status;

    private String orderCode;

    private int orderId;

    private int accountId;
}
