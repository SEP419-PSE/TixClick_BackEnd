package com.pse.tixclick.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.exception.ErrorCode;
import com.pse.tixclick.payload.dto.ContractAndContractPaymentDTO;
import com.pse.tixclick.payload.dto.ContractDTO;
import com.pse.tixclick.payload.dto.ContractPaymentDTO;
import com.pse.tixclick.payload.entity.company.Contract;
import com.pse.tixclick.payload.entity.company.ContractDetail;
import com.pse.tixclick.payload.entity.entity_enum.*;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
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
    ContractPaymentRepository contractPaymentRepository;

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    CassoService cassoService;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    AppUtils appUtils;

    @Autowired
    ContractRepository contractRepository;

    @Override
    public ContractPaymentRequest getContractPayment(String transactionCode, int paymentId) {
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonResponse = cassoService.getTransactions(null, 1, 10, "DESC");

        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            if (root.has("data") && root.get("data").has("records")) {
                JsonNode records = root.get("data").get("records");

                transactionCode = transactionCode.trim();

                ContractPayment contractPayment = contractPaymentRepository.findById(paymentId)
                        .orElseThrow(() -> new AppException(ErrorCode.CONTRACT_PAYMENT_NOT_FOUND));

                var context = SecurityContextHolder.getContext();
                String userName = context.getAuthentication().getName();

                var account = accountRepository.findAccountByUserName(userName)
                        .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));

                for (JsonNode record : records) {
                    String description = record.get("description").asText().trim();
                    String extractedCode = description.length() >= 8 ? description.substring(0, 8) : description;

                    if (extractedCode.equalsIgnoreCase(transactionCode)) {
                        double amount = record.get("amount").asDouble();

                        // Cập nhật trạng thái thanh toán
                        contractPayment.setStatus(EContractPaymentStatus.PAID);
                        contractPayment.setPaymentDate(LocalDateTime.now());
                        contractPaymentRepository.save(contractPayment);

                        // Lưu thông tin giao dịch
                        Transaction transaction = new Transaction();
                        transaction.setAmount(amount);
                        transaction.setDescription(description);
                        transaction.setTransactionCode(transactionCode);
                        transaction.setType(ETransactionType.CONTRACT_PAYMENT);
                        transaction.setStatus(ETransactionStatus.SUCCESS);
                        transaction.setTransactionDate(LocalDateTime.now());
                        transaction.setContractPayment(contractPayment);
                        transaction.setAccount(account);
                        transactionRepository.save(transaction);

                        // Cập nhật trạng thái chi tiết hợp đồng
                        ContractDetail contractDetail = contractPayment.getContractDetail();
                        contractDetail.setStatus(EContractDetailStatus.PAID);
                        contractDetailRepository.save(contractDetail);

                        return new ContractPaymentRequest(transactionCode, true);
                    }
                }

                // Không tìm thấy transactionCode
                throw new AppException(ErrorCode.TRANSACTION_NOT_FOUND);
            } else {
                throw new AppException(ErrorCode.TRANSACTION_NOT_FOUND);
            }
        } catch (IOException e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public List<ContractAndContractPaymentDTO> getAllContractPaymentByContract() {
        if (!appUtils.getAccountFromAuthentication().getRole().getRoleName().equals(ERole.MANAGER)) {
            throw new AppException(ErrorCode.NOT_PERMISSION);
        }

        List<Contract> contracts = contractRepository.findContractsByAccount_AccountId(appUtils.getAccountFromAuthentication().getAccountId());

        if (contracts.isEmpty()) {
            throw new AppException(ErrorCode.CONTRACT_NOT_FOUND);
        }
        List<ContractAndContractPaymentDTO> contractAndContractPaymentDTOs = new ArrayList<>();

        for(Contract contract : contracts) {
            List<ContractDetail> contractDetails = contractDetailRepository.findByContractId(contract.getContractId());
            if (contractDetails.isEmpty()) {
                throw new AppException(ErrorCode.CONTRACT_DETAIL_NOT_FOUND);
            }
            ContractDTO contractDTO = new ContractDTO();
            contractDTO.setContractId(contract.getContractId());
            contractDTO.setContractName(contract.getContractName());
            contractDTO.setTotalAmount(contract.getTotalAmount());
            contractDTO.setCommission(contract.getCommission());
            contractDTO.setContractType(contract.getContractType());
            contractDTO.setStartDate(contract.getStartDate());
            contractDTO.setEndDate(contract.getEndDate());
            contractDTO.setStatus(contract.getStatus().toString());
            contractDTO.setAccountId(contract.getAccount().getAccountId());
            contractDTO.setEventId(contract.getEvent().getEventId());
            contractDTO.setCompanyId(contract.getCompany().getCompanyId());
            contractDTO.setContractCode(contract.getContractCode());


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
                contractPaymentDTO.setStatus(String.valueOf(contractPayment.getStatus()));
                contractPaymentDTO.setNote(contractPayment.getNote());
                contractPaymentDTO.setContractDetailId(contractPayment.getContractDetail().getContractDetailId());
                contractPaymentDTO.setAccountNumber(contractPayment.getContractDetail().getContract().getCompany().getBankingCode());
                contractPaymentDTO.setBankName(contractPayment.getContractDetail().getContract().getCompany().getBankingName());
                contractPaymentDTOS.add(contractPaymentDTO);
            }

            ContractAndContractPaymentDTO contractAndContractPaymentDTO = new ContractAndContractPaymentDTO();
            contractAndContractPaymentDTO.setContractDTO(contractDTO);
            contractAndContractPaymentDTO.setContractPaymentDTOList(contractPaymentDTOS);

            contractAndContractPaymentDTOs.add(contractAndContractPaymentDTO);
        }
        return contractAndContractPaymentDTOs;
    }
}
