package com.pse.tixclick.service;

import com.pse.tixclick.payload.dto.ContractDTO;
import com.pse.tixclick.payload.request.create.CreateContractRequest;

public interface ContractService {
    ContractDTO createContract(CreateContractRequest request);
}
