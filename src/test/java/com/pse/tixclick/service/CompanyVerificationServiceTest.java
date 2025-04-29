package com.pse.tixclick.service;

import com.pse.tixclick.email.EmailService;
import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.exception.ErrorCode;
import com.pse.tixclick.payload.dto.CompanyVerificationDTO;
import com.pse.tixclick.payload.entity.Account;
import com.pse.tixclick.payload.entity.Notification;
import com.pse.tixclick.payload.entity.Role;
import com.pse.tixclick.payload.entity.company.Company;
import com.pse.tixclick.payload.entity.company.CompanyVerification;
import com.pse.tixclick.payload.entity.company.Member;
import com.pse.tixclick.payload.entity.entity_enum.*;
import com.pse.tixclick.repository.*;
import com.pse.tixclick.service.impl.CompanyVerificationServiceImpl;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CompanyVerificationServiceTest {

    @InjectMocks
    private CompanyVerificationServiceImpl companyVerificationService;

    @Mock
    private CompanyVerificationRepository companyVerificationRepository;
    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private SimpMessagingTemplate simpMessagingTemplate;
    @Mock
    private EmailService emailService;
    @Mock
    private ModelMapper modelMapper;

    private Company company;
    private CompanyVerification companyVerification;
    private Account representativeAccount;
    private Authentication authentication;
    private SecurityContext securityContext;
    private Role organizerRole;
    private Notification notification;

    @BeforeEach
    void setUp() {
        representativeAccount = new Account();
        representativeAccount.setUserName("testUser");
        representativeAccount.setFirstName("John");
        representativeAccount.setLastName("Doe");
        representativeAccount.setEmail("john.doe@example.com");

        company = new Company();
        company.setCompanyId(1);
        company.setRepresentativeId(representativeAccount);
        company.setStatus(ECompanyStatus.PENDING);

        companyVerification = new CompanyVerification();
        companyVerification.setCompanyVerificationId(10);
        companyVerification.setCompany(company);
        companyVerification.setAccount(representativeAccount);
        companyVerification.setStatus(EVerificationStatus.PENDING);
        companyVerification.setSubmitDate(LocalDateTime.now());

        authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("adminUser");

        securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        organizerRole = new Role();
        organizerRole.setRoleName(ERole.ORGANIZER);

        notification = new Notification();
        notification.setNotificationId(1);
        notification.setAccount(representativeAccount);
        notification.setMessage("Old notification");
        notification.setCreatedDate(LocalDateTime.now().minusDays(5));
    }

    @Test
    void approveCompanyVerification_Success() throws MessagingException {
        when(companyVerificationRepository.
                findCompanyVerificationByCompanyVerificationIdAndAccount_UserName(10, "adminUser"))
                .thenReturn(Optional.of(companyVerification));
        when(companyRepository.findById(1)).thenReturn(Optional.of(company));
        when(notificationRepository.countNotificationByAccountId("testUser")).thenReturn(5);
        when(accountRepository.findAccountByUserName("testUser")).thenReturn(Optional.of(representativeAccount));
        when(roleRepository.findRoleByRoleName(ERole.ORGANIZER)).thenReturn(Optional.of(organizerRole));
        when(modelMapper.map(companyVerification, CompanyVerificationDTO.class)).thenReturn(new CompanyVerificationDTO());

        CompanyVerificationDTO result = companyVerificationService.approveCompanyVerification(EVerificationStatus.APPROVED, 10);

        assertNotNull(result);
        assertEquals(EVerificationStatus.APPROVED, companyVerification.getStatus());
        assertEquals(ECompanyStatus.ACTIVE, company.getStatus());
        verify(notificationRepository, times(1)).save(any(Notification.class));
        verify(accountRepository, times(1)).save(representativeAccount);
        verify(memberRepository, times(1)).save(any(Member.class));
        verify(emailService, times(1)).sendAccountCreatedEmail(eq("john.doe@example.com"), eq("testUser"), eq("123456"), eq("John Doe"));
        verify(simpMessagingTemplate, times(1)).convertAndSendToUser(eq("testUser"), eq("/specific/messages"), eq("Your company has been approved"));
        verify(companyVerificationRepository, times(1)).save(companyVerification);
        verify(companyRepository, times(1)).save(company);
        verify(modelMapper, times(1)).map(companyVerification, CompanyVerificationDTO.class);
    }

    @Test
    void approveCompanyVerification_Success_DeletesOldestNotification() throws MessagingException {
        when(companyVerificationRepository.
                findCompanyVerificationByCompanyVerificationIdAndAccount_UserName(10, "adminUser"))
                .thenReturn(Optional.of(companyVerification));
        when(companyRepository.findById(1)).thenReturn(Optional.of(company));
        when(notificationRepository.countNotificationByAccountId("testUser")).thenReturn(10);
        when(notificationRepository.findTopByAccount_UserNameOrderByCreatedDateAsc("testUser"))
                .thenReturn(Optional.of(notification));
        when(accountRepository.findAccountByUserName("testUser")).thenReturn(Optional.of(representativeAccount));
        when(roleRepository.findRoleByRoleName(ERole.ORGANIZER)).thenReturn(Optional.of(organizerRole));
        when(modelMapper.map(companyVerification, CompanyVerificationDTO.class)).thenReturn(new CompanyVerificationDTO());

        CompanyVerificationDTO result = companyVerificationService.approveCompanyVerification(EVerificationStatus.APPROVED, 10);

        assertNotNull(result);
        verify(notificationRepository, times(1)).delete(notification);
        verify(notificationRepository, times(1)).save(any(Notification.class));
        verify(accountRepository, times(1)).save(representativeAccount);
        verify(memberRepository, times(1)).save(any(Member.class));
        verify(emailService, times(1)).sendAccountCreatedEmail(eq("john.doe@example.com"), eq("testUser"), eq("123456"), eq("John Doe"));
        verify(simpMessagingTemplate, times(1)).convertAndSendToUser(eq("testUser"), eq("/specific/messages"), eq("Your company has been approved"));
        verify(companyVerificationRepository, times(1)).save(companyVerification);
        verify(companyRepository, times(1)).save(company);
        verify(modelMapper, times(1)).map(companyVerification, CompanyVerificationDTO.class);
    }

    @Test
    void approveCompanyVerification_NotFoundCompanyVerification() {
        when(companyVerificationRepository.
                findCompanyVerificationByCompanyVerificationIdAndAccount_UserName(10, "adminUser"))
                .thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () ->
                companyVerificationService.approveCompanyVerification(EVerificationStatus.APPROVED, 10));

        assertEquals(ErrorCode.COMPANY_VERIFICATION_NOT_FOUND, exception.getErrorCode());
        verifyNoInteractions(companyRepository, notificationRepository, accountRepository, roleRepository, memberRepository, emailService, simpMessagingTemplate);
    }

    @Test
    void approveCompanyVerification_NotFoundCompany() {
        when(companyVerificationRepository.
                findCompanyVerificationByCompanyVerificationIdAndAccount_UserName(10, "adminUser"))
                .thenReturn(Optional.of(companyVerification));
        when(companyRepository.findById(1)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () ->
                companyVerificationService.approveCompanyVerification(EVerificationStatus.APPROVED, 10));

        assertEquals(ErrorCode.COMPANY_NOT_FOUND, exception.getErrorCode());
        verifyNoInteractions(notificationRepository, accountRepository, roleRepository, memberRepository, emailService, simpMessagingTemplate);
        verify(companyVerificationRepository, never()).save(any());
    }

    @Test
    void approveCompanyVerification_NotFoundOldestNotification() {
        when(companyVerificationRepository.
                findCompanyVerificationByCompanyVerificationIdAndAccount_UserName(10, "adminUser"))
                .thenReturn(Optional.of(companyVerification));
        when(companyRepository.findById(1)).thenReturn(Optional.of(company));
        when(notificationRepository.countNotificationByAccountId("testUser")).thenReturn(10);
        when(notificationRepository.findTopByAccount_UserNameOrderByCreatedDateAsc("testUser"))
                .thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () ->
                companyVerificationService.approveCompanyVerification(EVerificationStatus.APPROVED, 10));

        assertEquals(ErrorCode.NOTIFICATION_NOT_EXISTED, exception.getErrorCode());
        verify(notificationRepository, never()).delete(any());
        verifyNoInteractions(accountRepository, roleRepository, memberRepository, emailService, simpMessagingTemplate);
        verify(companyVerificationRepository, never()).save(any());
        verify(companyRepository, never()).save(any());
    }





    @Test
    void rejectCompanyVerification_Success() throws MessagingException {
        when(companyVerificationRepository.
                findCompanyVerificationByCompanyVerificationIdAndAccount_UserName(10, "adminUser"))
                .thenReturn(Optional.of(companyVerification));
        when(companyRepository.findById(1)).thenReturn(Optional.of(company));
        when(modelMapper.map(companyVerification, CompanyVerificationDTO.class)).thenReturn(new CompanyVerificationDTO());

        CompanyVerificationDTO result = companyVerificationService.approveCompanyVerification(EVerificationStatus.REJECTED, 10);

        assertNotNull(result);
        assertEquals(EVerificationStatus.REJECTED, companyVerification.getStatus());
        assertEquals(ECompanyStatus.REJECTED, company.getStatus());
        verify(companyVerificationRepository, times(1)).save(companyVerification);
        verify(companyRepository, times(1)).save(company);
        verify(modelMapper, times(1)).map(companyVerification, CompanyVerificationDTO.class);
        verifyNoInteractions(notificationRepository, accountRepository, roleRepository, memberRepository, emailService, simpMessagingTemplate);
    }

    @Test
    void approveCompanyVerification_PendingStatus_ThrowsException() {
        when(companyVerificationRepository.
                findCompanyVerificationByCompanyVerificationIdAndAccount_UserName(10, "adminUser"))
                .thenReturn(Optional.of(companyVerification));
        when(companyRepository.findById(1)).thenReturn(Optional.of(company));

        AppException exception = assertThrows(AppException.class, () ->
                companyVerificationService.approveCompanyVerification(EVerificationStatus.PENDING, 10));

        assertEquals(ErrorCode.COMPANY_VERIFICATION_PENDING, exception.getErrorCode());
        assertEquals(ECompanyStatus.PENDING, company.getStatus());
        assertEquals(EVerificationStatus.PENDING, companyVerification.getStatus());
        verify(companyRepository, never()).save(any());
        verify(companyVerificationRepository, never()).save(any());
        verifyNoInteractions(notificationRepository, accountRepository, roleRepository, memberRepository, emailService, simpMessagingTemplate, modelMapper);
    }


}