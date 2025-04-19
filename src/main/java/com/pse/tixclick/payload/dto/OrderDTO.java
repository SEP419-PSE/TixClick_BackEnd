package com.pse.tixclick.payload.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderDTO {
    private int orderId;

    private String orderCode;

    private String status;

    private double totalAmount;

    private int accountId;
    private String voucherCode;
}
