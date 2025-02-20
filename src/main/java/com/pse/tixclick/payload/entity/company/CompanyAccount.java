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
public class CompanyAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int companyAccountId;

    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(columnDefinition = "NVARCHAR(255)", nullable = false)
    private String username;

    @Column(columnDefinition = "NVARCHAR(255)", nullable = false)
    private String password;


}
