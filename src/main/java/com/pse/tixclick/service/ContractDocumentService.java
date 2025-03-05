package com.pse.tixclick.service;

import com.pse.tixclick.payload.dto.ContractDocumentDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ContractDocumentService {
    ContractDocumentDTO uploadContractDocument(MultipartFile file, int contractId) throws IOException;
}
