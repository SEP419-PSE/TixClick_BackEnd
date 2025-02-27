package com.pse.tixclick.service.impl;

import com.pse.tixclick.cloudinary.CloudinaryService;
import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.exception.ErrorCode;
import com.pse.tixclick.payload.dto.ContractDocumentDTO;
import com.pse.tixclick.payload.entity.company.ContractDocument;
import com.pse.tixclick.repository.AccountRepository;
import com.pse.tixclick.repository.ContractDocumentRepository;
import com.pse.tixclick.repository.ContractRepository;
import com.pse.tixclick.service.ContractDocumentService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ContractDocumentServiceImpl implements ContractDocumentService {
    ContractDocumentRepository contractDocumentRepository;
    AccountRepository accountRepository;
    CloudinaryService cloudinaryService;
    ContractRepository contractRepository;
    ModelMapper modelMapper;
    @Override
    public ContractDocumentDTO uploadContractDocument(MultipartFile file, int contractId) throws IOException {
        var context = SecurityContextHolder.getContext();
        String username = context.getAuthentication().getName();

        var account = accountRepository.findAccountByUserName(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        String fileURL = cloudinaryService.uploadDocumentToCloudinary(file);

        var contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new AppException(ErrorCode.CONTRACT_NOT_FOUND));

        var contractDocument = new ContractDocument();
        contractDocument.setContract(contract);
        contractDocument.setFileName(file.getOriginalFilename());
        contractDocument.setFileURL(fileURL);
        contractDocument.setFileType(file.getContentType());
        contractDocument.setUploadedBy(account);
        contractDocument.setUploadDate(java.time.LocalDateTime.now());
        contractDocumentRepository.save(contractDocument);

        return modelMapper.map(contractDocument, ContractDocumentDTO.class);

    }
}
