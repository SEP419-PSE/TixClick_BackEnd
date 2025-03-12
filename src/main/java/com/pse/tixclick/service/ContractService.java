package com.pse.tixclick.service;

import com.pse.tixclick.payload.dto.ContractDTO;
import com.pse.tixclick.payload.request.create.CreateContractRequest;

import java.util.List;

public interface ContractService {
    ContractDTO createContract(CreateContractRequest request);

    List<ContractDTO> getAllContracts();
}
