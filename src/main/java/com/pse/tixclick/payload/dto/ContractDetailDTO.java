package com.pse.tixclick.payload.dto;

import com.pse.tixclick.payload.entity.company.Contract;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ContractDetailDTO {
    private int contractDetailId;

    private String contractName;

    private String contractCode;

    private String contractDescription;

    private double contractAmount;

    private LocalDate contractPayDate;

    private String status;

    private int contractId;
}
