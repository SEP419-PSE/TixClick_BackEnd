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

    private int companyId;

    private int accountId;

    private String username;

    private String password;
}
