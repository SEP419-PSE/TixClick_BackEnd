package com.pse.tixclick.payload.entity.company;

import com.pse.tixclick.payload.entity.Account;
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

    @Column
    private String logoURL;

    @Column
    private String description;

    @Column(columnDefinition = "NVARCHAR(255)", nullable = false)
    private String status;

    @ManyToOne
    @JoinColumn(name = "representative_id", nullable = false)
    private Account representativeId;

}
