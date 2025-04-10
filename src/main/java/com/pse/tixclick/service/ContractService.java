package com.pse.tixclick.service;

import com.pse.tixclick.payload.dto.ContractAndDocumentsDTO;
import com.pse.tixclick.payload.dto.ContractDTO;
import com.pse.tixclick.payload.entity.entity_enum.EVerificationStatus;
import com.pse.tixclick.payload.request.create.CreateContractRequest;
import com.pse.tixclick.payload.response.ContractAndContractDetailResponse;
import jakarta.mail.MessagingException;

import java.util.List;

public interface ContractService {
    ContractDTO createContract(CreateContractRequest request);

    List<ContractAndDocumentsDTO> getAllContracts();

    String approveContract(int contractId, EVerificationStatus status) throws MessagingException;

    ContractAndContractDetailResponse createContractAndContractDetail(ContractAndContractDetailResponse request);
}
