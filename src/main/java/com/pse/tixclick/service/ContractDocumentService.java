package com.pse.tixclick.service;

import com.pse.tixclick.payload.dto.ContractDocumentDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface ContractDocumentService {
    ContractDocumentDTO uploadContractDocument(MultipartFile file, int contractId) throws IOException;

    ContractDocumentDTO getContractDocument(int contractId);

    List<ContractDocumentDTO> getContractDocumentsByContract(int contractId);

    void deleteContractDocument(int contractDocumentId) throws IOException;

    List<ContractDocumentDTO> getAllContractDocuments();

    List<ContractDocumentDTO> getContractDocumentsByEvent(int eventId);

    List<ContractDocumentDTO> getContractDocumentsByCompany(int companyId);

    File signPdf(String pdfUrl, String name) throws Exception;
}
