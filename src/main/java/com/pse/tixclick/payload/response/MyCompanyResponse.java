package com.pse.tixclick.payload.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class MyCompanyResponse {
    private int companyId;
    private String companyName;
    private String logoURL;
    private String address;
    private String subRole;
    private String representativeId;
}
