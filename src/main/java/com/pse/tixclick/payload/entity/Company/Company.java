package com.pse.tixclick.payload.entity.Company;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Collection;

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

    @Column(columnDefinition = "NVARCHAR(255)", nullable = false)
    private String status;

    @Column(columnDefinition = "NVARCHAR(255)", nullable = false)
    private String createdBy;

    @OneToMany(mappedBy = "company")
    private Collection<Member> members;
}
