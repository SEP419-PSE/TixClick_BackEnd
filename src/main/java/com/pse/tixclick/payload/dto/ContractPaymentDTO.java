package com.pse.tixclick.payload.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pse.tixclick.payload.entity.company.ContractDetail;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ContractPaymentDTO {
    private int contractPaymentId;

    private double paymentAmount;

    private LocalDateTime paymentDate;

    private String paymentMethod;

    private String status;

    private String note;

    private int contractDetailId;
}
