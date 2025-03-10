package com.pse.tixclick.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.pse.tixclick.cloudinary.CloudinaryService;
import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.exception.ErrorCode;
import com.pse.tixclick.payload.dto.CompanyDocumentDTO;
import com.pse.tixclick.payload.entity.company.CompanyDocuments;
import com.pse.tixclick.payload.entity.entity_enum.EVerificationStatus;
import com.pse.tixclick.payload.request.create.CreateCompanyDocumentRequest;
import com.pse.tixclick.repository.AccountRepository;
import com.pse.tixclick.repository.CompanyDocumentRepository;
import com.pse.tixclick.repository.CompanyRepository;
import com.pse.tixclick.repository.CompanyVerificationRepository;
import com.pse.tixclick.service.CompanyDocumentService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CompanyDocumentServiceImpl implements CompanyDocumentService {
    CompanyDocumentRepository companyDocumentRepository;
    AccountRepository accountRepository;
    CompanyRepository companyRepository;
    ModelMapper modelMapper;
    CloudinaryService cloudinary;
    CompanyVerificationRepository companyVerificationRepository;
    @Override
    public List<CompanyDocumentDTO> createCompanyDocument(CreateCompanyDocumentRequest createCompanyDocumentRequest, List<MultipartFile> files) {
        var context = SecurityContextHolder.getContext();
        String username = context.getAuthentication().getName();

        var companyVerification = companyVerificationRepository.findById(createCompanyDocumentRequest.getCompanyVerificationId())
                .orElseThrow(() -> new AppException(ErrorCode.COMPANY_VERIFICATION_NOT_FOUND));

        var company = companyRepository.findCompanyByCompanyIdAndRepresentativeId_UserName(
                        createCompanyDocumentRequest.getCompanyId(), username)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_OWNER));

        List<CompanyDocumentDTO> documentDTOList = new ArrayList<>();
        final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (MultipartFile file : files) {
            try {
                // Kiểm tra dung lượng file
                if (file.getSize() > MAX_FILE_SIZE) {
                    throw new AppException(ErrorCode.FILE_TOO_LARGE);
                }

                // Upload file lên Cloudinary thông qua CloudinaryService
                String fileURL = cloudinary.uploadDocumentToCloudinary(file);

                // Tạo đối tượng CompanyDocuments
                var companyDocument = new CompanyDocuments();
                companyDocument.setCompany(company);
                companyDocument.setFileName(file.getOriginalFilename());
                companyDocument.setFileURL(fileURL);
                companyDocument.setFileType(file.getContentType());
                companyDocument.setUploadDate(LocalDateTime.parse(createCompanyDocumentRequest.getUploadDate(), formatter));
                companyDocument.setStatus(true);
                // Lưu vào database
                companyDocumentRepository.save(companyDocument);

                // Chuyển đổi sang DTO và thêm vào danh sách
                documentDTOList.add(modelMapper.map(companyDocument, CompanyDocumentDTO.class));

            } catch (IOException e) {
                throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
            }
        }
        companyVerification.setStatus(EVerificationStatus.REVIEWING);
        companyVerificationRepository.save(companyVerification);
        return documentDTOList;
    }

    @Override
    public boolean deleteCompanyDocument(int companyDocumentId) {
//        var companyDocument = companyDocumentRepository.findById(companyDocumentId)
//                .orElseThrow(() -> new AppException(ErrorCode.COMPANY_DOCUMENT_NOT_FOUND));
//
//        // Xóa file trên Cloudinary
//        cloudinary.deleteDocumentFromCloudinary(companyDocument.getFileURL());
//
//        // Xóa trong database
//        companyDocumentRepository.delete(companyDocument);

        return true;
    }


}
