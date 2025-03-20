package com.pse.tixclick.service;

import com.pse.tixclick.payload.dto.ContractPaymentDTO;

import java.util.List;

public interface ContractPaymentService {
    String payContractPayment(int contractPaymentId);

    List<ContractPaymentDTO> getAllContractPaymentByContract(int contractId);
}
