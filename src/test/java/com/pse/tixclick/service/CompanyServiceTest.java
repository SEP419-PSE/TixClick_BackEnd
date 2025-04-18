package com.pse.tixclick.service;

import com.pse.tixclick.cloudinary.CloudinaryService;
import com.pse.tixclick.email.EmailService;
import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.exception.ErrorCode;
import com.pse.tixclick.payload.dto.CompanyDocumentDTO;
import com.pse.tixclick.payload.dto.CompanyVerificationDTO;
import com.pse.tixclick.payload.entity.Account;
import com.pse.tixclick.payload.entity.Notification;
import com.pse.tixclick.payload.entity.company.Company;
import com.pse.tixclick.payload.entity.company.CompanyVerification;
import com.pse.tixclick.payload.request.create.CreateCompanyRequest;
import com.pse.tixclick.payload.request.create.CreateCompanyVerificationRequest;
import com.pse.tixclick.payload.response.CompanyAndDocumentResponse;
import com.pse.tixclick.repository.AccountRepository;
import com.pse.tixclick.repository.CompanyRepository;
import com.pse.tixclick.repository.NotificationRepository;
import com.pse.tixclick.service.impl.CompanyServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private CompanyServiceImpl companyService;


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
        // ✅ Mock SecurityContext để tránh NullPointerException
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("nonexistentUser");

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // ✅ Mock không tìm thấy user
        when(accountRepository.findAccountByUserName("nonexistentUser")).thenReturn(Optional.empty());

        CreateCompanyRequest createCompanyRequest = new CreateCompanyRequest(
                "Company A", "123456", "companyA@example.com", "Owner Card",
                "Bank Name", "Bank Code", "National ID", "Company Address", "Description");

        AppException exception = assertThrows(AppException.class, () -> {
            companyService.createCompanyAndDocument(createCompanyRequest, logoURL, companyDocuments);
        });

        assertEquals(ErrorCode.USER_NOT_EXISTED, exception.getErrorCode());
    }

    @Test
    void testCreateCompanyAndDocument_ThrowException_WhenManagerNotFound() throws IOException, MessagingException {
        // Given
        CreateCompanyRequest createCompanyRequest = new CreateCompanyRequest();
        createCompanyRequest.setCompanyName("Test Company");
        // ... có thể mock thêm các field khác nếu cần

        MultipartFile logoFile = Mockito.mock(MultipartFile.class);
        List<MultipartFile> documentFiles = List.of();

        String username = "user_test";

        Account mockAccount = new Account();
        mockAccount.setUserName(username);
        mockAccount.setAccountId(1);

        // Mock context
        Authentication authentication = Mockito.mock(Authentication.class);
        when(authentication.getName()).thenReturn(username);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(accountRepository.findAccountByUserName(username)).thenReturn(Optional.of(mockAccount));
        when(cloudinary.uploadImageToCloudinary(any())).thenReturn("http://cloudinary.com/logo.jpg");

        // Trả về null để test case
        when(accountRepository.findManagerWithLeastVerifications()).thenReturn(null);

        // When & Then
        AppException exception = assertThrows(AppException.class, () -> {
            companyService.createCompanyAndDocument(createCompanyRequest, logoFile, documentFiles);
        });

        assertEquals(ErrorCode.MANAGER_NOT_FOUND, exception.getErrorCode());
    }

    // Test Case 3: Kiểm tra khi tài khoản đã là manager
    @Test
    void testCreateCompanyAndDocument_Success() throws IOException, MessagingException, jakarta.mail.MessagingException {
        // ✅ Mock SecurityContextHolder để lấy được username hiện tại
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("userA");

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);

        // ✅ Tạo account mock của user hiện tại
        Account account = new Account();
        account.setAccountId(1);
        account.setUserName("userA");

        when(accountRepository.findAccountByUserName("userA")).thenReturn(Optional.of(account));

        // ✅ Tạo account manager để gán vào verification
        Account manager = new Account();
        manager.setAccountId(2);
        manager.setUserName("managerA");

        when(accountRepository.findManagerWithLeastVerifications()).thenReturn(Optional.of(manager));

        // ✅ Mock cloudinary upload logo
        when(cloudinary.uploadImageToCloudinary(any())).thenReturn("uploadedLogoURL");

        // ✅ Mock save company
        Company company = new Company();
        company.setCompanyId(100);
        when(companyRepository.save(any())).thenReturn(company);

        // ✅ Mock tạo CompanyVerificationDTO
        CompanyVerificationDTO companyVerificationDTO = new CompanyVerificationDTO();
        companyVerificationDTO.setCompanyVerificationId(10);
        companyVerificationDTO.setCompanyId(100);
        companyVerificationDTO.setSubmitById(2);
        companyVerificationDTO.setSubmitDate(LocalDateTime.now());
        companyVerificationDTO.setNote("Pending verification");
        companyVerificationDTO.setStatus("PENDING");

        when(companyVerificationService.createCompanyVerification(any(CreateCompanyVerificationRequest.class)))
                .thenReturn(companyVerificationDTO);

        // ✅ Mock document trả về đúng dữ liệu DTO
        List<CompanyDocumentDTO> documentDTOList = List.of(
                new CompanyDocumentDTO(1, 100, "document1.pdf", "http://cloudinary.com/document1.pdf", "pdf", LocalDateTime.now())
        );

        when(companyDocumentService.createCompanyDocument(any(), anyList())).thenReturn(documentDTOList);

        // ✅ Mock email + messaging
        doNothing().when(emailService).sendCompanyCreationRequestNotification(any(), any(), any());
        doNothing().when(messagingTemplate).convertAndSendToUser(any(), anyString(), anyString());

        // ✅ Mock notificationRepository trả về Optional<Notification>
        Notification mockNotification = new Notification();
        mockNotification.setNotificationId(1);
        mockNotification.setMessage("Test notification");
        mockNotification.setAccount(manager); // Giả sử notification này liên quan đến manager
        mockNotification.setCreatedDate(LocalDateTime.now());

        when(notificationRepository.findTopByAccount_UserNameOrderByCreatedDateAsc("managerA"))
                .thenReturn(Optional.of(mockNotification));

        // ✅ Mock countNotificationByAccountId trả về count >= 10
        when(notificationRepository.countNotificationByAccountId("managerA")).thenReturn(10);

        // ✅ Tạo request giả lập
        CreateCompanyRequest createCompanyRequest = new CreateCompanyRequest(
                "Company A", "123456", "companyA@example.com", "Owner Card",
                "Bank Name", "Bank Code", "National ID", "Company Address", "Description");

        // ✅ Gọi hàm cần test
        CompanyAndDocumentResponse response = companyService.createCompanyAndDocument(createCompanyRequest, logoURL, companyDocuments);

        // ✅ Kiểm tra kết quả
        assertNotNull(response);
        assertEquals("Company A", response.getCreateCompanyResponse().getCompanyName());
        assertEquals("uploadedLogoURL", response.getCreateCompanyResponse().getLogoURL());

        assertNotNull(response.getCreateCompanyDocumentResponse());
        assertEquals(1, response.getCreateCompanyDocumentResponse().size());

        CompanyDocumentDTO doc = response.getCreateCompanyDocumentResponse().get(0);
        assertEquals("document1.pdf", doc.getFileName());
        assertEquals("http://cloudinary.com/document1.pdf", doc.getFileURL());
        assertEquals("pdf", doc.getFileType());

        // ✅ Kiểm tra xem notification có bị xóa hay không
        verify(notificationRepository, times(1)).delete(mockNotification);  // Kiểm tra xem phương thức delete có được gọi không
    }




}
