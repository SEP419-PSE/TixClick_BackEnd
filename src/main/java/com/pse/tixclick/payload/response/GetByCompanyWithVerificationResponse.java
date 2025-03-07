package com.pse.tixclick.payload.response;

import com.pse.tixclick.payload.entity.entity_enum.ECompanyStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GetByCompanyWithVerificationResponse {
    private int companyId;
    private String companyName;
    private String codeTax;
    private String bankingName;
    private String bankingCode;
    private String nationalId;
    private String logoURL;
    private String address;
    private String description;

    private int companyVerificationId;
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
