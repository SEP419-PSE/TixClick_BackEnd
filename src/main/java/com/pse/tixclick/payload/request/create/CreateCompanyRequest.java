package com.pse.tixclick.payload.request.create;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateCompanyRequest {
    private String companyName;
    private String codeTax;
    private String ownerCard;
    private String bankingName;
    private String bankingCode;
    private String nationalId;
    private String address;
    private String description;
}
