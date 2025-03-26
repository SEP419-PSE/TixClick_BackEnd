package com.pse.tixclick.service.impl;

import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.exception.ErrorCode;
import com.pse.tixclick.payload.dto.ContractDTO;
import com.pse.tixclick.payload.dto.PaymentDTO;
import com.pse.tixclick.payload.entity.company.Contract;
import com.pse.tixclick.payload.entity.entity_enum.EEventStatus;
import com.pse.tixclick.payload.request.create.CreateContractRequest;
import com.pse.tixclick.payload.response.QRCompanyResponse;
import com.pse.tixclick.repository.AccountRepository;
import com.pse.tixclick.repository.CompanyRepository;
import com.pse.tixclick.repository.ContractRepository;
import com.pse.tixclick.repository.EventRepository;
import com.pse.tixclick.service.ContractService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ContractServiceImpl implements ContractService {
    ContractRepository contractRepository;
    AccountRepository accountRepository;
    EventRepository eventRepository;
    CompanyRepository companyRepository;
    ModelMapper modelMapper;

    @Override
    public ContractDTO createContract(CreateContractRequest request) {
        var context = SecurityContextHolder.getContext();
        String userName = context.getAuthentication().getName();

        var manager = accountRepository.findAccountByUserName(userName)
                .orElseThrow(()-> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (!"MANAGER".equals(manager.getRole().getRoleName())) {
            throw new AppException(ErrorCode.ROLE_NOT_EXISTED);
        }


        var event = eventRepository.findEventByEventId(request.getEventId())
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        if(event.getStatus() != EEventStatus.PENDING){
            throw new AppException(ErrorCode.STATUS_NOT_CORRECT);
        }

        var company = companyRepository.findCompanyByCompanyId(event.getCompany().getCompanyId())
                .orElseThrow(() -> new AppException(ErrorCode.COMPANY_NOT_FOUND));

        Contract newContract = new Contract();
        newContract.setContractType(request.getContractType());
        newContract.setEvent(event);
        newContract.setAccount(manager);
        newContract.setCompany(company);
        newContract.setCommission(request.getCommission());
        newContract.setTotalAmount(request.getTotalAmount());

        contractRepository.save(newContract);

        return modelMapper.map(newContract,ContractDTO.class);
    }

    @Override
    public List<ContractDTO> getAllContracts() {
        List<Contract> contracts = contractRepository.findAll();
        return contracts.stream()
                .map(contract -> modelMapper.map(contract, ContractDTO.class))
                .toList();    }


}
