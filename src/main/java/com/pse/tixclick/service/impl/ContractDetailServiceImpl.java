package com.pse.tixclick.service.impl;

import com.pse.tixclick.email.EmailService;
import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.exception.ErrorCode;
import com.pse.tixclick.payload.dto.ContractDetailDTO;
import com.pse.tixclick.payload.entity.company.Contract;
import com.pse.tixclick.payload.entity.company.ContractDetail;
import com.pse.tixclick.payload.entity.entity_enum.EContractDetailStatus;
import com.pse.tixclick.payload.entity.entity_enum.EContractPaymentStatus;
import com.pse.tixclick.payload.entity.entity_enum.ERole;
import com.pse.tixclick.payload.entity.payment.ContractPayment;
import com.pse.tixclick.payload.request.create.ContractDetailRequest;
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
    ModelMapper modelMapper;

    @Autowired
    AppUtils appUtils;


    @Override
    public List<ContractDetailDTO> createContractDetail(CreateContractDetailRequest createContractDetailRequests) {
        if (!appUtils.getAccountFromAuthentication().getRole().getRoleName().equals(ERole.MANAGER)) {
            throw new AppException(ErrorCode.NOT_PERMISSION);
        }

        Contract contract = contractRepository
                .findById(createContractDetailRequests.getContractId())
                .orElseThrow(() -> new AppException(ErrorCode.CONTRACT_NOT_FOUND));

        List<ContractDetailDTO> contractDetailDTOList = new ArrayList<>();

        for (ContractDetailRequest contractDetailRequest : createContractDetailRequests.getContractDetails()) {
            ContractDetail contractDetail = new ContractDetail();
            contractDetail.setContractDetailName(contractDetailRequest.getContractDetailName());
            contractDetail.setDescription(contractDetailRequest.getContractDetailDescription());
            contractDetail.setAmount(contractDetailRequest.getContractDetailAmount());
            contractDetail.setPayDate(contractDetailRequest.getContractDetailPayDate());
            contractDetail.setContract(contract);
            contractDetail.setContractDetailCode(contractDetailRequest.getContractDetailCode());
            contractDetail.setStatus(EContractDetailStatus.PENDING);
            contractDetail.setPercentage(contractDetailRequest.getContractDetailPercentage());
            contractDetail = contractDetailRepository.save(contractDetail);

            ContractPayment contractPayment = new ContractPayment();
            contractPayment.setPaymentAmount(contractDetailRequest.getContractDetailAmount());
            contractPayment.setContractDetail(contractDetail);
            contractPayment.setNote(contractDetailRequest.getContractDetailDescription());
            contractPayment.setPaymentMethod("Thanh Toan Ngan Hang");
            contractPayment.setStatus(EContractPaymentStatus.PENDING);
            contractPaymentRepository.save(contractPayment);

            ContractDetailDTO contractDetailDTO = new ContractDetailDTO();
            contractDetailDTO.setContractDetailId(contractDetail.getContractDetailId());
            contractDetailDTO.setContractDetailName(contractDetail.getContractDetailName());
            contractDetailDTO.setContractDetailCode(contractDetail.getContractDetailCode());
            contractDetailDTO.setDescription(contractDetail.getDescription());
            contractDetailDTO.setContractAmount(contractDetail.getAmount());
            contractDetailDTO.setContractPayDate(contractDetail.getPayDate());
            contractDetailDTO.setStatus(String.valueOf(contractDetail.getStatus()));
            contractDetailDTO.setContractId(contract.getContractId());

            contractDetailDTOList.add(contractDetailDTO);
        }

        return contractDetailDTOList;
    }

    @Override
    public List<ContractDetailDTO> getAllContractDetailByContract(int contractId) {
        if(appUtils.getAccountFromAuthentication() == null){
            throw new AppException(ErrorCode.NEEDED_LOGIN);
        }
        else if (!appUtils.getAccountFromAuthentication().getRole().getRoleName().equals(ERole.MANAGER) || !appUtils.getAccountFromAuthentication().getRole().getRoleName().equals(ERole.ORGANIZER)) {
            throw new AppException(ErrorCode.NOT_PERMISSION);
        }

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
        var contract = contractDetailRepository
                .findById(contractDetailId)
                .orElseThrow(() -> new AppException(ErrorCode.CONTRACT_DETAIL_NOT_FOUND));

        List<ContractDetail> contractDetails = contractDetailRepository.findByContractId(contract.getContract().getContractId());

        for (ContractDetail contractDetail : contractDetails) {
            if(contractDetail.getStatus().equals(EContractDetailStatus.PAID)){
                continue;
            }
            var company = contractDetail.getContract().getCompany();
            String description = String.format("TIXCLICK %s - THANH TOAN HOP DONG %s",
                    contractDetail.getContractDetailCode(), contractDetail.getContractDetailName());


            return QRCompanyResponse.builder()
                    .bankID(company.getBankingName())
                    .accountID(company.getBankingCode())
                    .dueDate(contractDetail.getPayDate())
                    .amount(contractDetail.getAmount())
                    .status(contractDetail.getStatus())
                    .description(description)
                    .build();

        }

        throw new AppException(ErrorCode.CONTRACT_DETAIL_NOT_FOUND);



    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void updateAmountOfContractPayment() throws MessagingException {
        List<Integer> eventIds = eventRepository.findScheduledEventIds();

        for (Integer eventId : eventIds) {
            Double totalAmount = transactionRepository.getTotalAmountByEventId(eventId);
            Contract contract = contractRepository.findByEventId(eventId);

            List<ContractDetail> contractDetails = contractDetailRepository.findByContractId(contract.getContractId());
            for (ContractDetail contractDetail : contractDetails) {
                if(contractDetail.getStatus() == EContractDetailStatus.PAID) {
                    continue; // Bỏ qua nếu đã thanh toán
                }
                if(contractDetail.getStatus() == EContractDetailStatus.OVERDUE) {
                    continue; // Bỏ qua nếu đã quá hạn
                }
                double newTotalAmount = totalAmount * contractDetail.getPercentage();

                ContractPayment contractPayment = contractPaymentRepository
                        .findByContractDetailId(contractDetail.getContractDetailId())
                        .orElseThrow(() -> new AppException(ErrorCode.CONTRACT_PAYMENT_NOT_FOUND));

                LocalDate today = LocalDate.now();
                LocalDate payDate = contractDetail.getPayDate();
                LocalDate threeDaysBefore = payDate.minusDays(3);

                // Gửi cảnh báo trước 3 ngày
                if (threeDaysBefore.isEqual(today)) {
                    emailService.sendContractPaymentWarningToManager(
                            contract.getAccount().getEmail(),
                            contract.getCompany().getCompanyName(),
                            newTotalAmount,
                            payDate,
                            contract.getContractId(),
                            contractDetail.getContractDetailId(),
                            contractDetail.getContractDetailName(),
                            contract.getEvent().getEventName()
                    );
                }
                // Nếu quá hạn, set status OVERDUE
                if (payDate.isBefore(today)) {
                    contractDetail.setStatus(EContractDetailStatus.OVERDUE); // giả định bạn có enum này
                    contractDetailRepository.save(contractDetail);
                }

                contractPayment.setPaymentAmount(newTotalAmount);
                contractPaymentRepository.save(contractPayment);
            }
        }
    }
}
