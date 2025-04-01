package com.pse.tixclick.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pse.tixclick.email.EmailService;
import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.exception.ErrorCode;
import com.pse.tixclick.payload.dto.ContractPaymentDTO;
import com.pse.tixclick.payload.entity.company.ContractDetail;
import com.pse.tixclick.payload.entity.entity_enum.EContractDetailStatus;
import com.pse.tixclick.payload.entity.entity_enum.ETransactionType;
import com.pse.tixclick.payload.entity.payment.ContractPayment;
import com.pse.tixclick.payload.entity.payment.Transaction;
import com.pse.tixclick.payload.request.ContractPaymentRequest;
import com.pse.tixclick.payment.CassoService;
import com.pse.tixclick.repository.*;
import com.pse.tixclick.service.ContractPaymentService;
import com.pse.tixclick.utils.AppUtils;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ContractPaymentServiceImpl implements ContractPaymentService {
    @Autowired
    ContractDetailRepository contractDetailRepository;

    @Autowired
    ContractRepository contractRepository;

    @Autowired
    ContractPaymentRepository contractPaymentRepository;

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    EventRepository eventRepository;

    @Autowired
    EmailService emailService;

    @Autowired
    AppUtils appUtils;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    CassoService cassoService;

    @Autowired
    AccountRepository accountRepository;

    @Override
    public ContractPaymentRequest getContractPayment(String transactionCode, int paymentId) {
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonResponse = cassoService.getTransactions(null, 1, 10, "DESC");
        HashMap<String, JsonNode> transactionMap = new HashMap<>();

        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            if (root.has("data") && root.get("data").has("records")) {
                JsonNode records = root.get("data").get("records");

                for (JsonNode record : records) {
                    String description = record.get("description").asText();
                    String extractedCode = description.length() >= 5 ? description.substring(0, 5) : description;

                    System.out.println("Extracted Code: '" + extractedCode + "'");
                    transactionMap.put(extractedCode, record);
                }

                transactionCode = transactionCode.trim();

                ContractPayment contractPayment = contractPaymentRepository.findById(paymentId)
                        .orElseThrow(() -> new AppException(ErrorCode.CONTRACT_PAYMENT_NOT_FOUND));

                var context = SecurityContextHolder.getContext();
                String userName = context.getAuthentication().getName();

                var account = accountRepository.findAccountByUserName(userName)
                        .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));

                if (transactionMap.containsKey(transactionCode)) {
                    JsonNode matchedTransaction = transactionMap.get(transactionCode);
                    double amount = matchedTransaction.get("amount").asDouble();
                    String description = matchedTransaction.get("description").asText();


                    contractPayment.setStatus("PAID");
                    contractPayment.setPaymentDate(LocalDateTime.now());
                    contractPaymentRepository.save(contractPayment);
                    // Lưu vào database
                    Transaction transaction = new Transaction();
                    transaction.setAmount(amount);
                    transaction.setDescription(description);
                    transaction.setTransactionCode(transactionCode);
                    transaction.setType("PAYMENT");
                    transaction.setStatus("SUCCESS");
                    transaction.setTransactionDate(LocalDateTime.now());
                    transaction.setContractPayment(contractPayment);
                    transaction.setAccount(account);
                    transactionRepository.save(transaction);

                    // Tự động chuyển hướng sang trang success
                    return new ContractPaymentRequest(transactionCode, true);
                } else {
                    return new ContractPaymentRequest(transactionCode, false);
                }
            } else {
                return new ContractPaymentRequest(transactionCode, false);
            }
        } catch (IOException e) {
            return new ContractPaymentRequest(transactionCode, false);
        }
    }



    @Override
    public List<ContractPaymentDTO> getAllContractPaymentByContract(int contractId) {
        List<ContractDetail> contractDetails = contractDetailRepository.findByContractId(contractId);
        if (contractDetails.isEmpty()) {
            throw new AppException(ErrorCode.CONTRACT_DETAIL_NOT_FOUND);
        }

        List<ContractPayment> contractPayments = new ArrayList<>();
        for (ContractDetail detail : contractDetails) {
            ContractPayment contractPayment = contractPaymentRepository
                    .findByContractDetailId(detail.getContractDetailId())
                    .orElseThrow(() -> new AppException(ErrorCode.CONTRACT_PAYMENT_NOT_FOUND));
            contractPayments.add(contractPayment);
        }

        List<ContractPaymentDTO> contractPaymentDTOS = new ArrayList<>();
        for (ContractPayment contractPayment : contractPayments) {
            ContractPaymentDTO contractPaymentDTO = new ContractPaymentDTO();
            contractPaymentDTO.setContractPaymentId(contractPayment.getContractPaymentId());
            contractPaymentDTO.setPaymentAmount(contractPayment.getPaymentAmount());
            contractPaymentDTO.setPaymentDate(contractPayment.getPaymentDate());
            contractPaymentDTO.setPaymentMethod(contractPayment.getPaymentMethod());
            contractPaymentDTO.setStatus(contractPayment.getStatus());
            contractPaymentDTO.setNote(contractPayment.getNote());
            contractPaymentDTO.setContractDetailId(contractPayment.getContractDetail().getContractDetailId());
            contractPaymentDTO.setAccountNumber(contractPayment.getContractDetail().getContract().getCompany().getBankingCode());
            contractPaymentDTO.setBankName(contractPayment.getContractDetail().getContract().getCompany().getBankingName());
            contractPaymentDTOS.add(contractPaymentDTO);
        }

        return contractPaymentDTOS;
    }
}
