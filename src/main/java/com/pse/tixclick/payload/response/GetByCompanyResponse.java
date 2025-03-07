package com.pse.tixclick.payload.response;

import com.pse.tixclick.payload.dto.AccountDTO;
import com.pse.tixclick.payload.dto.CompanyDTO;
import com.pse.tixclick.payload.entity.entity_enum.ECompanyStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GetByCompanyResponse {
    private int companyId;
    private String companyName;
    private String codeTax;
    private String bankingName;
    private String bankingCode;
    private String nationalId;
    private String logoURL;
    private String description;
    private ECompanyStatus status;
    private CustomAccount customAccount;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class CustomAccount {
        private String lastName;
        private String firstName;
        private String email;
        private String phoneNumber;
    }
}
