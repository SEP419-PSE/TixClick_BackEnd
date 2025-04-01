package com.pse.tixclick.service.impl;

import com.pse.tixclick.email.EmailService;
import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.exception.ErrorCode;
import com.pse.tixclick.payload.dto.CompanyVerificationDTO;
import com.pse.tixclick.payload.entity.Notification;
import com.pse.tixclick.payload.entity.Role;
import com.pse.tixclick.payload.entity.company.CompanyVerification;
import com.pse.tixclick.payload.entity.company.Member;
import com.pse.tixclick.payload.entity.entity_enum.*;
import com.pse.tixclick.payload.request.create.CreateCompanyVerificationRequest;
import com.pse.tixclick.repository.*;
import com.pse.tixclick.service.CompanyVerificationService;
import com.pse.tixclick.utils.AppUtils;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CompanyVerificationServiceImpl implements CompanyVerificationService {
    CompanyVerificationRepository companyVerificationRepository;
    CompanyRepository companyRepository;
    AccountRepository accountRepository;
    ModelMapper modelMapper;
    MemberRepository memberRepository;
    EmailService emailService;
    AppUtils appUtils;
    SimpMessagingTemplate simpMessagingTemplate;
    NotificationRepository notificationRepository;
    RoleRepository roleRepository;
    @Override
    public CompanyVerificationDTO createCompanyVerification(CreateCompanyVerificationRequest createCompanyVerificationRequest) {

        var account = accountRepository.findManagerWithLeastVerifications()
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));


        var company = companyRepository.findById(createCompanyVerificationRequest.getCompanyId())

                .orElseThrow(() -> new AppException(ErrorCode.COMPANY_NOT_FOUND));

        var companyVerification = new CompanyVerification();
        companyVerification.setCompany(company);
        companyVerification.setSubmitDate(createCompanyVerificationRequest.getSubmitDate());
        companyVerification.setStatus(createCompanyVerificationRequest.getStatus());
        companyVerification.setAccount(account);
        companyVerificationRepository.save(companyVerification);
        return  modelMapper.map(companyVerification, CompanyVerificationDTO.class);


    }

    @Override
    public CompanyVerificationDTO approveCompanyVerification(EVerificationStatus status, int companyVerificationId) throws MessagingException {
        var context = SecurityContextHolder.getContext();
        String username = context.getAuthentication().getName();

        CompanyVerification companyVerification = companyVerificationRepository.
                findCompanyVerificationByCompanyVerificationIdAndAccount_UserName(companyVerificationId, username)
                .orElseThrow(() -> new AppException(ErrorCode.COMPANY_VERIFICATION_NOT_FOUND));

        var company = companyRepository.findById(companyVerification.getCompany().getCompanyId())
                .orElseThrow(() -> new AppException(ErrorCode.COMPANY_NOT_FOUND));


        switch (status) {
            case APPROVED:






                int count = notificationRepository.countNotificationByAccountId(company.getRepresentativeId().getUserName());
                log.info("Count: {}", count);

                if(count >= 10) {
                    Notification notification = notificationRepository.findTopByAccount_UserNameOrderByCreatedDateAsc(company.getRepresentativeId().getUserName())
                            .orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_NOT_EXISTED));
                    notificationRepository.delete(notification);
                }



                simpMessagingTemplate.convertAndSendToUser(company.getRepresentativeId().getUserName(), "/specific/messages",
                        "Your company has been approved");

                Notification notification = new Notification();
                notification.setAccount(company.getRepresentativeId());
                notification.setMessage("Your company has been approved");
                notification.setCreatedDate(LocalDateTime.now());
                notification.setRead(false);

                var account = accountRepository
                        .findAccountByUserName(company.getRepresentativeId().getUserName())
                        .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));

                Role role = roleRepository.findRoleByRoleName(ERole.ORGANIZER)
                        .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

                account.setRole(role);
                accountRepository.save(account);

                notificationRepository.save(notification);

                Member member = new Member();
                member.setCompany(company);
                member.setSubRole(ESubRole.OWNER);
                member.setAccount(company.getRepresentativeId());
                member.setStatus(EStatus.ACTIVE);
                memberRepository.save(member);

                String fullName = company.getRepresentativeId().getFirstName() + " " + company.getRepresentativeId().getLastName();

                emailService.sendAccountCreatedEmail(company.getRepresentativeId().getEmail(), company.getRepresentativeId().getUserName(), "123456", fullName);

                company.setStatus(ECompanyStatus.ACTIVE);
                break;

            case REJECTED:

                company.setStatus(ECompanyStatus.REJECTED);
                break;

            case PENDING:

                company.setStatus(ECompanyStatus.PENDING);
                break;

            default:

                company.setStatus(ECompanyStatus.INACTIVE);
                break;
        }

        companyRepository.save(company);

        // Cập nhật trạng thái xác minh
        companyVerification.setSubmitDate(LocalDateTime.now());
        companyVerification.setStatus(status);
        companyVerificationRepository.save(companyVerification);

        return modelMapper.map(companyVerification, CompanyVerificationDTO.class);
    }

    @Override
    public List<CompanyVerificationDTO> getCompanyVerificationsByManager() {
        var context = SecurityContextHolder.getContext();
        String username = context.getAuthentication().getName();
        List<CompanyVerification> companyVerificationList = companyVerificationRepository.findCompanyVerificationsByAccount_UserName(username);
        if(companyVerificationList.isEmpty()) {
            throw new AppException(ErrorCode.COMPANY_VERIFICATION_NOT_FOUND);
        }
        return companyVerificationList.stream()
                .map(companyVerification -> modelMapper.map(companyVerification, CompanyVerificationDTO.class))
                .toList();
    }

    @Override
    public CompanyVerificationDTO resubmitCompanyVerification(int companyVerificationId) {
        var context = SecurityContextHolder.getContext();
        String username = context.getAuthentication().getName();
        CompanyVerification companyVerification = companyVerificationRepository
                .findCompanyVerificationByCompanyVerificationIdAndAccount_UserName(companyVerificationId, username)
                .orElseThrow(() -> new AppException(ErrorCode.COMPANY_VERIFICATION_NOT_FOUND));
        if (companyVerification.getStatus() != EVerificationStatus.REJECTED) {
            throw new AppException(ErrorCode.COMPANY_VERIFICATION_NOT_REJECTED);
        }

        var account = accountRepository.findManagerWithLeastVerifications()
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        CompanyVerification resubmitCompanyVerification = new CompanyVerification();

        resubmitCompanyVerification.setCompany(companyVerification.getCompany());
        resubmitCompanyVerification.setAccount(account);
        resubmitCompanyVerification.setNote(companyVerification.getNote());
        resubmitCompanyVerification.setStatus(EVerificationStatus.PENDING);
        resubmitCompanyVerification.setSubmitDate(LocalDateTime.now());
        companyVerificationRepository.save(resubmitCompanyVerification);

        return modelMapper.map(resubmitCompanyVerification, CompanyVerificationDTO.class);
    }

}
