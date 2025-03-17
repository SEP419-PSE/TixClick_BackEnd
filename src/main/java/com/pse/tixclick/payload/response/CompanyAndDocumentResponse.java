package com.pse.tixclick.payload.response;

import com.pse.tixclick.payload.dto.CompanyDocumentDTO;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CompanyAndDocumentResponse {
    CreateCompanyResponse createCompanyResponse;
    List<CompanyDocumentDTO> createCompanyDocumentResponse;
}
