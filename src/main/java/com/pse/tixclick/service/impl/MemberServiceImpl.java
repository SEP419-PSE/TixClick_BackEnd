package com.pse.tixclick.service.impl;

import com.pse.tixclick.email.EmailService;
import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.exception.ErrorCode;
import com.pse.tixclick.payload.dto.MemberDTO;
import com.pse.tixclick.payload.entity.Account;
import com.pse.tixclick.payload.entity.Company.Company;
import com.pse.tixclick.payload.entity.Company.Member;
import com.pse.tixclick.payload.entity.entity_enum.ESubRole;
import com.pse.tixclick.payload.request.CreateMemberRequest;
import com.pse.tixclick.payload.response.MemberDTOResponse;
import com.pse.tixclick.repository.AccountRepository;
import com.pse.tixclick.repository.CompanyRepository;
import com.pse.tixclick.repository.MemberRepository;
import com.pse.tixclick.service.MemberService;
import jakarta.mail.MessagingException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MemberServiceImpl implements MemberService {
    AccountRepository accountRepository;
    CompanyRepository companyRepository;
    MemberRepository memberRepository;
    EmailService emailService;
    ModelMapper modelMapper;
    @Override
    public MemberDTOResponse createMember(CreateMemberRequest createMemberRequest) {
        Company company = companyRepository.findById(createMemberRequest.getCompanyId())
                .orElseThrow(() -> new AppException(ErrorCode.COMPANY_NOT_FOUND));

        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();
        Account account = accountRepository.findAccountByUserName(name)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        var member = memberRepository.findMemberByAccount_AccountIdAndCompany_CompanyId(account.getAccountId(), createMemberRequest.getCompanyId());

        if (member.isPresent()) {
            if (!(member.get().getSubRole().equals(ESubRole.OWNER.name()) || member.get().getSubRole().equals(ESubRole.ADMIN.name()))) {
                throw new AppException(ErrorCode.INVALID_ROLE);
            }
        } else {
            throw new AppException(ErrorCode.MEMBER_NOT_FOUND);
        }

        List<MemberDTO> createdMembers = new ArrayList<>();
        List<String> sentEmails = new ArrayList<>();
        for (String email : createMemberRequest.getMailList()) {
            boolean skipThisEmail = false;
            Account invitedAccount = null;
            try {
                // Check if the invited account exists
                invitedAccount = accountRepository.findAccountByEmail(email)
                        .orElseGet(() -> {
                            try {
                                emailService.sendOTPtoActiveAccount(email, company.getCompanyName(),"Haha");
                                String emailSentMessage = email + " đã gửi mail";
                                sentEmails.add(emailSentMessage);
                            } catch (MessagingException e) {
                                throw new RuntimeException(e);
                            }
                            return null;
                        });

                if (invitedAccount != null) {
                    var existingMember = memberRepository.findMemberByAccount_AccountIdAndCompany_CompanyId(
                            invitedAccount.getAccountId(), createMemberRequest.getCompanyId());

                    if (existingMember.isPresent()) {
                        log.error("Account with email " + email + " is already a member of the company.");
                        skipThisEmail = true;
                    }
                }

            } catch (Exception e) {
                log.error("Error processing email " + email + ": " + e.getMessage());
                skipThisEmail = true;
            }

            if (!skipThisEmail && invitedAccount != null) {
                // Create and save the new member for the company
                Member newMember = new Member();
                newMember.setSubRole(createMemberRequest.getSubRole());
                newMember.setCompany(company);
                newMember.setAccount(invitedAccount);

                memberRepository.save(newMember);

                // Add the created member to the response list
                MemberDTO memberDTO = new MemberDTO();
                memberDTO.setMemberId(newMember.getMemberId());
                memberDTO.setSubRole(newMember.getSubRole());
                memberDTO.setAccountId(newMember.getAccount().getAccountId());
                createdMembers.add(memberDTO);
            }
        }

        // Return the list of created members
        return new MemberDTOResponse(createdMembers,sentEmails); // A response class to return all created members
    }

}

