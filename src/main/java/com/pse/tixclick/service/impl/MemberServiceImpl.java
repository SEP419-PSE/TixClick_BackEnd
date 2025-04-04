package com.pse.tixclick.service.impl;

import com.pse.tixclick.email.EmailService;
import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.exception.ErrorCode;
import com.pse.tixclick.payload.dto.MemberDTO;
import com.pse.tixclick.payload.entity.Account;
import com.pse.tixclick.payload.entity.company.Company;
import com.pse.tixclick.payload.entity.company.Member;
import com.pse.tixclick.payload.entity.entity_enum.ECompanyStatus;
import com.pse.tixclick.payload.entity.entity_enum.ERole;
import com.pse.tixclick.payload.entity.entity_enum.EStatus;
import com.pse.tixclick.payload.entity.entity_enum.ESubRole;
import com.pse.tixclick.payload.request.create.CreateMemberRequest;
import com.pse.tixclick.payload.response.MemberDTOResponse;
import com.pse.tixclick.repository.AccountRepository;
import com.pse.tixclick.repository.CompanyRepository;
import com.pse.tixclick.repository.MemberRepository;
import com.pse.tixclick.repository.RoleRepository;
import com.pse.tixclick.service.MemberService;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class MemberServiceImpl implements MemberService {
    AccountRepository accountRepository;
    CompanyRepository companyRepository;
    MemberRepository memberRepository;
    EmailService emailService;
    ModelMapper modelMapper;
    StringRedisTemplate stringRedisTemplate;
    RoleRepository roleRepository;

    @Override
    public MemberDTOResponse createMember(CreateMemberRequest createMemberRequest) {
        Company company = companyRepository.findById(createMemberRequest.getCompanyId())
                .orElseThrow(() -> new AppException(ErrorCode.COMPANY_NOT_FOUND));
        if(!company.getStatus().equals(ECompanyStatus.ACTIVE)){
            throw new AppException(ErrorCode.COMPANY_INACTIVE);
        }
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();


        var member = memberRepository.findMemberByAccount_UserNameAndCompany_CompanyId(name, createMemberRequest.getCompanyId());

        if (member.isPresent()) {
            if (!(member.get().getSubRole().equals(ESubRole.OWNER) || member.get().getSubRole().equals(ESubRole.ADMIN))) {
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


                                String link = "http://localhost:5173/account/register";
                                emailService.sendAccountRegistrationToCompany(email, company.getCompanyName(), link);
                                String emailSentMessage = email + " đã gửi mail";
                                String key = "CREATE_MEMBER:" + email;
                                stringRedisTemplate.opsForValue().set(key, email, Duration.ofDays(7));
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
                newMember.setSubRole(ESubRole.valueOf(createMemberRequest.getSubRole()));
                newMember.setCompany(company);
                newMember.setAccount(invitedAccount);
                newMember.setStatus(EStatus.ACTIVE);
                memberRepository.save(newMember);

                invitedAccount.setRole(roleRepository.findRoleByRoleName(ERole.ORGANIZER)
                        .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND)));
                // Add the created member to the response list
                MemberDTO memberDTO = new MemberDTO();
                memberDTO.setMemberId(newMember.getMemberId());
                memberDTO.setSubRole(String.valueOf(newMember.getSubRole()));
                memberDTO.setAccountId(newMember.getAccount().getAccountId());
                memberDTO.setCompanyId(newMember.getCompany().getCompanyId());
                memberDTO.setStatus(String.valueOf(newMember.getStatus()));
                createdMembers.add(memberDTO);
            }
        }

        // Return the list of created members
        return new MemberDTOResponse(createdMembers,sentEmails); // A response class to return all created members
    }

    @Override
    public boolean deleteMember(int id) {
        Member deleteMember = memberRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND));
        Company company = companyRepository.findById(deleteMember.getCompany().getCompanyId())
                .orElseThrow(() -> new AppException(ErrorCode.COMPANY_NOT_FOUND));
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();
        Member member = memberRepository.findMemberByAccount_UserNameAndCompany_CompanyId(name, company.getCompanyId())
                .orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND));
        String subRole = String.valueOf(member.getSubRole());
        if (!subRole.equals(ESubRole.OWNER.name()) && !subRole.equals(ESubRole.ADMIN.name())) {
            throw new AppException(ErrorCode.INVALID_ROLE);
        }
        deleteMember.setStatus(EStatus.INACTIVE);
        memberRepository.save(deleteMember);
        return true;

    }

    @Override
    public List<MemberDTO> getMembersByCompanyId(int companyId) {
        List<Member> members = memberRepository.findMembersByCompany_CompanyId(companyId)
                .orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND));

        return members.stream()
                .map(member -> modelMapper.map(member, MemberDTO.class))
                .collect(Collectors.toList());
    }



}

