package com.pse.tixclick.service;

import com.pse.tixclick.cloudinary.CloudinaryService;
import com.pse.tixclick.email.EmailService;
import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.exception.ErrorCode;
import com.pse.tixclick.payload.dto.CompanyDocumentDTO;
import com.pse.tixclick.payload.entity.Account;
import com.pse.tixclick.payload.entity.company.Company;
import com.pse.tixclick.payload.entity.company.CompanyVerification;
import com.pse.tixclick.payload.request.create.CreateCompanyRequest;
import com.pse.tixclick.payload.request.create.CreateCompanyVerificationRequest;
import com.pse.tixclick.payload.response.CompanyAndDocumentResponse;
import com.pse.tixclick.repository.AccountRepository;
import com.pse.tixclick.repository.CompanyRepository;
import com.pse.tixclick.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private CompanyVerificationService companyVerificationService;

    @Mock
    private CloudinaryService cloudinary;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private EmailService emailService;

    @Mock
    private CompanyDocumentService companyDocumentService;

    @InjectMocks
    private CompanyService companyService;

    private MultipartFile logoURL;
    private List<MultipartFile> companyDocuments;

    @BeforeEach
    void setUp() {
        logoURL = new MockMultipartFile("logo", "logo.png", "image/png", new byte[0]);
        companyDocuments = List.of(new MockMultipartFile("document1", "doc1.pdf", "application/pdf", new byte[0]));
    }

    // Test Case 1: Kiểm tra khi người dùng không tồn tại
    @Test
    void testCreateCompanyAndDocument_UserNotExist() {
        String username = "nonexistentUser";
        when(accountRepository.findAccountByUserName(username)).thenReturn(Optional.empty());

        CreateCompanyRequest createCompanyRequest = new CreateCompanyRequest(
                "Company A", "123456", "companyA@example.com", "Owner Card",
                "Bank Name", "Bank Code", "National ID", "Company Address", "Description");

        AppException exception = assertThrows(AppException.class, () -> {
            companyService.createCompanyAndDocument(createCompanyRequest, logoURL, companyDocuments);
        });

        assertEquals(ErrorCode.USER_NOT_EXISTED, exception.getErrorCode());
    }

    // Test Case 2: Kiểm tra khi không tìm thấy manager
    @Test
    void testCreateCompanyAndDocument_ManagerNotFound() {
        Account account = new Account();
        account.setUserName("userA");

        when(accountRepository.findAccountByUserName(anyString())).thenReturn(Optional.of(account));
        when(accountRepository.findManagerWithLeastVerifications()).thenReturn(Optional.empty());

        CreateCompanyRequest createCompanyRequest = new CreateCompanyRequest(
                "Company A", "123456", "companyA@example.com", "Owner Card",
                "Bank Name", "Bank Code", "National ID", "Company Address", "Description");

        AppException exception = assertThrows(AppException.class, () -> {
            companyService.createCompanyAndDocument(createCompanyRequest, logoURL, companyDocuments);
        });

        assertEquals(ErrorCode.MANAGER_NOT_FOUND, exception.getErrorCode());
    }

    // Test Case 3: Kiểm tra khi tài khoản đã là manager
//    @Test
//    void testCreateCompanyAndDocument_Success() throws IOException, MessagingException, jakarta.mail.MessagingException {
//        Account account = new Account();
//        account.setUserName("userA");
//
//        // Mock tài khoản hiện tại
//        when(accountRepository.findAccountByUserName(anyString())).thenReturn(Optional.of(account));
//
//        // Mock trả về một manager hợp lệ
//        Account manager = new Account();
//        manager.setUserName("managerA");
//        when(accountRepository.findManagerWithLeastVerifications()).thenReturn(Optional.of(manager));
//
//        // Mock Cloudinary upload
//        when(cloudinary.uploadImageToCloudinary(any())).thenReturn("uploadedLogoURL");
//
//        // Mock các repository và service khác
//        Company company = new Company();
//        when(companyRepository.save(any())).thenReturn(company);
//
//        // Mock việc tạo company verification
//        CompanyVerification companyVerification = new CompanyVerification();
//        when(companyVerificationService.createCompanyVerification(any(CreateCompanyVerificationRequest.class))).thenReturn(companyVerification);
//
//        // Mock việc tạo company document
//        List<CompanyDocumentDTO> documentDTOList = List.of(new CompanyDocumentDTO(1, 1, "doc1", "url1", "pdf", LocalDateTime.now()));
//        when(companyDocumentService.createCompanyDocument(any(), anyList())).thenReturn(documentDTOList);
//
//        // Mock email
//        doNothing().when(emailService).sendCompanyCreationRequestNotification(any(), any(), any());
//
//        // Mock messaging
//        doNothing().when(messagingTemplate).convertAndSendToUser(any(), anyString(), anyString());
//
//        CreateCompanyRequest createCompanyRequest = new CreateCompanyRequest(
//                "Company A", "123456", "companyA@example.com", "Owner Card",
//                "Bank Name", "Bank Code", "National ID", "Company Address", "Description");
//
//        CompanyAndDocumentResponse response = companyService.createCompanyAndDocument(createCompanyRequest, logoURL, companyDocuments);
//
//        // Kiểm tra kết quả
//        assertNotNull(response);
//        assertEquals("Company A", response.getCreateCompanyResponse().getCompanyName());
//        assertEquals("uploadedLogoURL", response.getCreateCompanyResponse().getLogoURL());
//        assertEquals(1, response.getCreateCompanyDocumentResponse().size());
//    }
}
