package com.pse.tixclick.service;

import com.pse.tixclick.payload.dto.ContractDetailDTO;
import com.pse.tixclick.payload.request.create.CreateContractDetailRequest;

import java.util.List;

public interface ContractDetailService {
    List<ContractDetailDTO> createContractDetail(List<CreateContractDetailRequest> createContractDetailRequests, int contractId);

    List<ContractDetailDTO> getAllContractDetailByContract(int contractId);
}
