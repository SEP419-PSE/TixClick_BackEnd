package com.pse.tixclick.service.impl;

import com.pse.tixclick.email.EmailService;
import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.exception.ErrorCode;
import com.pse.tixclick.payload.dto.ContractDetailDTO;
import com.pse.tixclick.payload.entity.company.Contract;
import com.pse.tixclick.payload.entity.company.ContractDetail;
import com.pse.tixclick.payload.entity.entity_enum.EContractDetailStatus;
import com.pse.tixclick.payload.entity.payment.ContractPayment;
import com.pse.tixclick.payload.request.create.CreateContractDetailRequest;
import com.pse.tixclick.payload.response.QRCompanyResponse;
import com.pse.tixclick.repository.*;
import com.pse.tixclick.service.ContractDetailService;
import com.pse.tixclick.utils.AppUtils;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ContractDetailServiceImpl implements ContractDetailService {
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


    @Override
    public List<ContractDetailDTO> createContractDetail(List<CreateContractDetailRequest> createContractDetailRequests, int contractId) {
        Contract contract = contractRepository
                .findById(contractId)
                .orElseThrow(() -> new AppException(ErrorCode.CONTRACT_NOT_FOUND));

        List<ContractDetailDTO> contractDetailDTOList = new ArrayList<>();

        for (CreateContractDetailRequest createContractDetailRequest : createContractDetailRequests) {
            ContractDetail contractDetail = new ContractDetail();
            contractDetail.setContractDetailName(createContractDetailRequest.getContractDetailName());
            contractDetail.setDescription(createContractDetailRequest.getContractDetailDescription());
            contractDetail.setAmount(createContractDetailRequest.getContractDetailAmount());
            contractDetail.setPayDate(createContractDetailRequest.getContractDetailPayDate());
            contractDetail.setContract(contract);
            contractDetail.setContractDetailCode(createContractDetailRequest.getContractDetailCode());
            contractDetail.setStatus(EContractDetailStatus.PENDING.name());
            contractDetail.setPercentage(createContractDetailRequest.getContractDetailPercentage());
            contractDetail = contractDetailRepository.save(contractDetail);

            ContractPayment contractPayment = new ContractPayment();
            contractPayment.setPaymentAmount(createContractDetailRequest.getContractDetailAmount());
            contractPayment.setContractDetail(contractDetail);
            contractPayment.setNote(createContractDetailRequest.getContractDetailDescription());
            contractPayment.setPaymentMethod("Thanh Toan Ngan Hang");
            contractPayment.setStatus(EContractDetailStatus.PENDING.name());
            contractPaymentRepository.save(contractPayment);

            ContractDetailDTO contractDetailDTO = new ContractDetailDTO();
            contractDetailDTO.setContractDetailId(contractDetail.getContractDetailId());
            contractDetailDTO.setContractName(contractDetail.getContractDetailName());
            contractDetailDTO.setContractCode(contractDetail.getContractDetailCode());
            contractDetailDTO.setContractDescription(contractDetail.getDescription());
            contractDetailDTO.setContractAmount(contractDetail.getAmount());
            contractDetailDTO.setContractPayDate(contractDetail.getPayDate());
            contractDetailDTO.setStatus(contractDetail.getStatus());
            contractDetailDTO.setContractId(contract.getContractId());

            contractDetailDTOList.add(contractDetailDTO);
        }

        return contractDetailDTOList;
    }

    @Override
    public List<ContractDetailDTO> getAllContractDetailByContract(int contractId) {
        List<ContractDetail> contractDetails = contractDetailRepository.findByContractId(contractId);
        if(contractDetails.isEmpty()) {
            throw new AppException(ErrorCode.CONTRACT_DETAIL_NOT_FOUND);
        }

        return contractDetails.stream()
                .map(contractDetail -> modelMapper.map(contractDetail, ContractDetailDTO.class))
                .toList();
    }

    @Override
    public QRCompanyResponse getQRCompany(int contractDetailId) {
        var contractDetail = contractDetailRepository.findById(contractDetailId)
                .orElseThrow(() -> new AppException(ErrorCode.CONTRACT_NOT_FOUND));

        var contract = contractDetail.getContract();
        if (contract == null) {
            throw new AppException(ErrorCode.CONTRACT_NOT_FOUND);
        }

        var company = contract.getCompany();
        if (company == null) {
            throw new AppException(ErrorCode.COMPANY_NOT_FOUND);
        }

        // Kiểm tra các giá trị không được null
        if (contractDetail.getContractDetailCode() == null) {
            throw new AppException(ErrorCode.CONTRACT_DETAIL_CODE_NOT_FOUND);
        }

        if (contract.getContractName() == null) {
            throw new AppException(ErrorCode.CONTRACT_NAME_NOT_FOUND);
        }

        if (company.getBankingName() == null) {
            throw new AppException(ErrorCode.BANKING_NAME_NOT_FOUND);
        }

        if (company.getBankingCode() == null) {
            throw new AppException(ErrorCode.BANKING_CODE_NOT_FOUND);
        }

        String description = String.format("TIXCLICK %s - THANH TOAN HOP DONG %s",
                contractDetail.getContractDetailCode(), contract.getContractName());

        return QRCompanyResponse.builder()
                .bankID(company.getBankingName())
                .accountID(company.getBankingCode())
                .amount(contractDetail.getAmount()) // Đảm bảo amount hợp lệ
                .description(description)
                .build();
    }




    @Scheduled(cron = "0 0 0 * * ?")
    public void updateAmountOfContractPayment() throws MessagingException {
        List<Integer> eventIds = eventRepository.findScheduledEventIds();

        for (Integer eventId : eventIds) {
            Double totalAmount = transactionRepository.getTotalAmountByEventId(eventId);
            Contract contract = contractRepository.findByEventId(eventId);

            List<ContractDetail> contractDetails = contractDetailRepository.findByContractId(contract.getContractId());
            for (ContractDetail contractDetail : contractDetails) {
                double newTotalAmount = totalAmount * contractDetail.getPercentage();

                LocalDate threeDaysBefore = contractDetail.getPayDate().minusDays(3);
                if (threeDaysBefore.isEqual(LocalDate.now())) {
                    emailService.sendContractPaymentWarningToManager(contract.getAccount().getEmail(), contract.getCompany().getCompanyName(), newTotalAmount, contractDetail.getPayDate(), contract.getContractId(), contractDetail.getContractDetailId(), contractDetail.getContractDetailName(), contract.getEvent().getEventName());
                }
                ContractPayment contractPayment = contractPaymentRepository
                        .findByContractDetailId(contractDetail.getContractDetailId())
                        .orElseThrow(() -> new AppException(ErrorCode.CONTRACT_PAYMENT_NOT_FOUND));

                contractPayment.setPaymentAmount(newTotalAmount);
                contractPaymentRepository.save(contractPayment);
             }
        }
    }
}
