package com.pse.tixclick.service;

import com.pse.tixclick.payload.dto.ContractDTO;
import com.pse.tixclick.payload.entity.entity_enum.EVerificationStatus;
import com.pse.tixclick.payload.request.create.CreateContractRequest;
import jakarta.mail.MessagingException;

import java.util.List;

public interface ContractService {
    ContractDTO createContract(CreateContractRequest request);

    List<ContractDTO> getAllContracts();

    String approveContract(int contractVerificationId, EVerificationStatus status) throws MessagingException;
}
