package com.pse.tixclick.payload.dto;

import com.pse.tixclick.payload.entity.Account;
import com.pse.tixclick.payload.entity.company.Company;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CompanyAccountDTO {
    private int companyAccountId;

    private Company company;

    private Account account;

    private String username;

    private String password;
}
