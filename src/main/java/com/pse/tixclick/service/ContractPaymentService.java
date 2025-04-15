package com.pse.tixclick.service;

import com.pse.tixclick.payload.dto.ContractAndContractPaymentDTO;
import com.pse.tixclick.payload.dto.ContractPaymentDTO;
import com.pse.tixclick.payload.request.ContractPaymentRequest;

import java.util.List;

public interface ContractPaymentService {
    ContractPaymentRequest getContractPayment(String transactionCode, int paymentId);

    List<ContractAndContractPaymentDTO> getAllContractPaymentByContract();


}
