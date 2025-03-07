package com.pse.tixclick.payload.entity.company;

import com.pse.tixclick.payload.entity.Account;
import com.pse.tixclick.payload.entity.entity_enum.ECompanyStatus;
import com.pse.tixclick.payload.entity.entity_enum.EStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int companyId;

    @Column(columnDefinition = "NVARCHAR(255)", nullable = false)
    private String companyName;

    @Column(columnDefinition = "NVARCHAR(255)", nullable = false)
    private String codeTax;

    @Column(columnDefinition = "NVARCHAR(255)", nullable = false)
    private String bankingName;

    @Column(columnDefinition = "NVARCHAR(255)", nullable = false)
    private String bankingCode;

    @Column(columnDefinition = "NVARCHAR(255)", nullable = false)
    private String nationalId;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String address;

    @Column
    private String logoURL;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    ECompanyStatus status;

    @ManyToOne
    @JoinColumn(name = "representative_id", nullable = false)
    private Account representativeId;

}
