package com.pse.tixclick.payload.request.update;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateCompanyRequest {
    private String companyName;
    private String codeTax;
    private String bankingName;
    private String bankingCode;
    private String email;
    private String nationalId;
    private String logoURL;
    private String address;
    private String description;
}
