package com.pse.tixclick.payload.entity.company;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pse.tixclick.payload.entity.Account;
import com.pse.tixclick.payload.entity.payment.ContractPayment;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "contract_details")
@Entity
public class ContractDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int contractDetailId;

    @Column
    private String contractDetailName;

    @Column
    private String contractDetailCode;

    @Column
    private String description;

    @Column
    private double amount;

    @Column
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDate payDate;

    @Column
    private String status;

    @Column
    private double percentage;

    @ManyToOne
    @JoinColumn(name="contract_id", nullable = false)
    private Contract contract;

    @OneToOne(mappedBy = "contractDetail", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ContractPayment contractPayment;
}
