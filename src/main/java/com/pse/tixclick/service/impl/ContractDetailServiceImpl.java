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
            contractDetailDTO.setContractDescription(contractDetail.getDescription());
            contractDetailDTO.setContractAmount(contractDetail.getAmount());
            contractDetailDTO.setContractPayDate(contractDetail.getPayDate());
            contractDetailDTO.setStatus(contractDetail.getStatus());
            contractDetailDTO.setContractId(contract.getContractId());

            contractDetailDTOList.add(contractDetailDTO);
        }

        return contractDetailDTOList;
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
                ContractPayment contractPayment = contractPaymentRepository.findByContractDetailId(contractDetail.getContractDetailId());
                contractPayment.setPaymentAmount(newTotalAmount);
                contractPaymentRepository.save(contractPayment);
             }
        }
    }
}
