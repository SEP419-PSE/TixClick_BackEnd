package com.pse.tixclick.payload.entity.company;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pse.tixclick.payload.entity.Account;
import com.pse.tixclick.payload.entity.entity_enum.CompanyVerificationStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class CompanyVerification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int companyVerificationId;

    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    Company company;

    @ManyToOne
    @JoinColumn(name = "submit_by_id", nullable = false)
    Account account;

    @Column
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime submitDate;

    @Column
    String note;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    CompanyVerificationStatus status;
}
