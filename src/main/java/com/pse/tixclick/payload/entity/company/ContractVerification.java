package com.pse.tixclick.payload.entity.company;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pse.tixclick.payload.entity.Account;
import com.pse.tixclick.payload.entity.entity_enum.EVerificationStatus;
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
public class ContractVerification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int contractVerificationId;

    @ManyToOne
    @JoinColumn(name = "contract_id", nullable = false)
    Contract contract;

    @ManyToOne
    @JoinColumn(name = "verified_by_id", nullable = false)
    Account account;

    @Column
    String note;

    @Column
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime verifyDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EVerificationStatus status;


}
