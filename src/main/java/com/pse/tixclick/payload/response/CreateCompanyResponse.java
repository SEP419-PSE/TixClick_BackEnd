package com.pse.tixclick.payload.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateCompanyResponse {
    private int companyId;
    private String companyName;
    private String codeTax;
    private String bankingName;
    private String bankingCode;
    private String nationalId;
    private String logoURL;
    private String address;
    private String description;
    private String status;
    private int representativeId;

    private int companyVerificationId;
}
