package com.pse.tixclick.service;

import com.pse.tixclick.payload.dto.ContractPaymentDTO;

import java.util.List;

public interface ContractPaymentService {
    String payContractPayment(String transactionCode, int paymentId);

    List<ContractPaymentDTO> getAllContractPaymentByContract(int contractId);
}
