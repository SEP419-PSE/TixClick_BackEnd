package com.pse.tixclick.payload.entity.payment;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pse.tixclick.payload.entity.company.Contract;
import com.pse.tixclick.payload.entity.company.ContractDetail;
import com.pse.tixclick.payload.entity.entity_enum.EContractPaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class ContractPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int contractPaymentId;

    @Column(nullable = false)
    private double paymentAmount;

    @Column
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime paymentDate;

    @Column(nullable = false)
    private String paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EContractPaymentStatus status;

    @Column(nullable = false, columnDefinition = "NVARCHAR(255)")
    private String note;

    @OneToOne
    @JoinColumn(name = "contract_detail_id", nullable = false)
    private ContractDetail contractDetail;
}
