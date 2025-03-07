package com.pse.tixclick.payload.response;

import com.pse.tixclick.payload.dto.AccountDTO;
import com.pse.tixclick.payload.dto.CompanyDTO;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GetByCompanyResponse {
    private CompanyDTO companyDTO;
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
