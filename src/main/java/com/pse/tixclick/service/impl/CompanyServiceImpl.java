package com.pse.tixclick.service.impl;

import com.pse.tixclick.cloudinary.CloudinaryService;
import com.pse.tixclick.email.EmailService;
import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.exception.ErrorCode;
import com.pse.tixclick.payload.dto.CompanyDTO;
import com.pse.tixclick.payload.dto.CompanyDocumentDTO;
import com.pse.tixclick.payload.dto.ContractDetailDTO;
import com.pse.tixclick.payload.entity.Account;
import com.pse.tixclick.payload.entity.Notification;
import com.pse.tixclick.payload.entity.company.*;
import com.pse.tixclick.payload.entity.entity_enum.*;
import com.pse.tixclick.payload.request.create.CreateCompanyDocumentRequest;
import com.pse.tixclick.payload.request.create.CreateCompanyRequest;
import com.pse.tixclick.payload.request.create.CreateCompanyVerificationRequest;
import com.pse.tixclick.payload.request.update.UpdateCompanyRequest;
import com.pse.tixclick.payload.response.*;
import com.pse.tixclick.repository.*;
import com.pse.tixclick.service.CompanyDocumentService;
import com.pse.tixclick.service.CompanyService;
import com.pse.tixclick.service.CompanyVerificationService;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CompanyServiceImpl implements CompanyService {
    CompanyRepository companyRepository;
    AccountRepository accountRepository;
    ModelMapper modelMapper;
    CompanyVerificationService companyVerificationService;
    CloudinaryService cloudinary;
    CompanyVerificationRepository companyVerificationRepository;
    EmailService emailService;
    SimpMessagingTemplate messagingTemplate;
    CompanyDocumentService companyDocumentService;
    NotificationRepository notificationRepository;
    CompanyDocumentRepository companyDocumentRepository;
    ContractRepository contractRepository;
    ContractDetailRepository contractDetailRepository;
    MemberRepository memberRepository;
    EventRepository eventRepository;
    @Override
    public CreateCompanyResponse createCompany(CreateCompanyRequest createCompanyRequest, MultipartFile file) throws IOException, MessagingException {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        var account = accountRepository.findAccountByUserName(name)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        String logoURL = cloudinary.uploadImageToCloudinary(file);

        Company company = new Company();
        company.setCompanyName(createCompanyRequest.getCompanyName());
        company.setDescription(createCompanyRequest.getDescription());
        company.setRepresentativeId(account);
        company.setCodeTax(createCompanyRequest.getCodeTax());
        company.setBankingCode(createCompanyRequest.getBankingCode());
        company.setBankingName(createCompanyRequest.getBankingName());
        company.setNationalId(createCompanyRequest.getNationalId());
        company.setLogoURL(logoURL);
        company.setEmail(createCompanyRequest.getEmail());
        company.setAddress(createCompanyRequest.getAddress());
        company.setStatus(ECompanyStatus.PENDING);
        companyRepository.save(company);

        CreateCompanyVerificationRequest createCompanyVerificationRequest = new CreateCompanyVerificationRequest();
        createCompanyVerificationRequest.setCompanyId(company.getCompanyId());
        createCompanyVerificationRequest.setStatus(EVerificationStatus.PENDING);

        var companyVerification = companyVerificationService.createCompanyVerification(createCompanyVerificationRequest);
        var companyManager = accountRepository.findById(companyVerification.getSubmitById())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_MANAGER_IN_DATABASE));
        String fullname = (companyManager.getFirstName() != null ? companyManager.getFirstName() : "") +
                " " +
                (companyManager.getLastName() != null ? companyManager.getLastName() : "");
        fullname = fullname.trim(); // Loại bỏ khoảng trắng thừa nếu có
        String notificationMessage = "Công ty mới cần duyệt: " + company.getCompanyName();
        List<Account> managers = accountRepository.findAccountsByRole_RoleId(4);

        log.info("Sending notification to user: {}", companyManager.getUserName());
        messagingTemplate.convertAndSendToUser(companyManager.getUserName(), "/queue/notifications", notificationMessage);

        Notification notification = new Notification();
        notification.setAccount(companyManager);
        notification.setMessage(notificationMessage);
        notification.setRead(false);
        notification.setCreatedDate(LocalDateTime.now());
        notificationRepository.save(notification);

        emailService.sendCompanyCreationRequestNotification(companyManager.getEmail(), company.getCompanyName(), fullname);

        // Tạo đối tượng response
        return new CreateCompanyResponse(
                company.getCompanyId(),
                company.getCompanyName(),
                company.getCodeTax(),
                company.getBankingName(),
                company.getBankingCode(),
                company.getOwnerCard(),
                company.getEmail(),
                company.getNationalId(),
                company.getLogoURL(),
                company.getAddress(),
                company.getDescription(),
                company.getStatus().name(),
                account.getAccountId(),
                companyVerification.getCompanyVerificationId()
        );
    }


    @Override
    public CompanyDTO updateCompany(UpdateCompanyRequest updateCompanyRequest, int id) {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        Company company = companyRepository.findCompanyByCompanyIdAndRepresentativeId_UserName(id, name)
                .orElseThrow(() -> new AppException(ErrorCode.COMPANY_NOT_EXISTED));

        company.setCompanyName(updateCompanyRequest.getCompanyName());
        company.setDescription(updateCompanyRequest.getDescription());
        company.setCodeTax(updateCompanyRequest.getCodeTax());
        company.setBankingCode(updateCompanyRequest.getBankingCode());
        company.setBankingName(updateCompanyRequest.getBankingName());
        company.setEmail(updateCompanyRequest.getEmail());
        company.setNationalId(updateCompanyRequest.getNationalId());
        company.setLogoURL(updateCompanyRequest.getLogoURL());
        company.setAddress(updateCompanyRequest.getAddress());
        companyRepository.save(company);

        return modelMapper.map(company, CompanyDTO.class);
    }

    @Override
    public String approveCompany(int id) {
        Company company = companyRepository
                .findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.COMPANY_NOT_EXISTED));

        company.setStatus(ECompanyStatus.ACTIVE);
        companyRepository.save(company);
        return "Company is now active";    }

    @Override
    public String rejectCompany(int id) {
        Company company = companyRepository
                .findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.COMPANY_NOT_EXISTED));

        company.setStatus(ECompanyStatus.REJECTED);
        companyRepository.save(company);
        return "Company is now rejected";
    }

    @Override
    public String inactiveCompany(int id) {
        Company company = companyRepository
                .findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.COMPANY_NOT_EXISTED));

        company.setStatus(ECompanyStatus.INACTIVE);
        companyRepository.save(company);
        return "Company is now inactive";
    }

    @Override
    public List<GetByCompanyResponse> getAllCompany() {
        List<Company> companies = companyRepository.findAll();

        // Lấy danh sách các accountId từ companies (lọc null)
        List<Integer> accountIds = companies.stream()
                .map(company -> company.getRepresentativeId() != null ? company.getRepresentativeId().getAccountId() : null)
                .filter(accountId -> accountId != null)
                .toList();

        // Lấy danh sách Account một lần để tránh N+1 Query
        Map<Integer, Account> accountMap = accountRepository.findAllById(accountIds)
                .stream()
                .collect(Collectors.toMap(Account::getAccountId, account -> account));

        return companies.stream()
                .map(company -> {
                    // Lấy account từ map thay vì query riêng lẻ
                    Account account = (company.getRepresentativeId() != null)
                            ? accountMap.get(company.getRepresentativeId().getAccountId())
                            : null;

                    // Tạo CustomAccount
                    GetByCompanyResponse.CustomAccount customAccount = (account != null)
                            ? new GetByCompanyResponse.CustomAccount(
                            account.getFirstName(),
                            account.getLastName(),
                            account.getEmail(),
                            account.getPhone()
                    )
                            : null;

                    return new GetByCompanyResponse(
                            company.getCompanyId(),
                            company.getCompanyName(),
                            company.getCodeTax(),
                            company.getBankingName(),
                            company.getBankingCode(),
                            company.getNationalId(),
                            company.getLogoURL(),
                            company.getAddress(), // Thêm address
                            company.getDescription(),
                            company.getStatus(),
                            customAccount
                    );
                })
                .toList();
    }



    @Override
    public GetByCompanyResponse getCompanyById(int id) {
        Company company = companyRepository
                .findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.COMPANY_NOT_EXISTED));

        GetByCompanyResponse getByCompanyResponse = new GetByCompanyResponse();
        getByCompanyResponse.setCompanyId(company.getCompanyId());
        getByCompanyResponse.setCompanyName(company.getCompanyName());
        getByCompanyResponse.setCodeTax(company.getCodeTax());
        getByCompanyResponse.setBankingName(company.getBankingName());
        getByCompanyResponse.setBankingCode(company.getBankingCode());
        getByCompanyResponse.setNationalId(company.getNationalId());
        getByCompanyResponse.setLogoURL(company.getLogoURL());
        getByCompanyResponse.setDescription(company.getDescription());
        getByCompanyResponse.setStatus(company.getStatus());

        getByCompanyResponse.setCustomAccount(new GetByCompanyResponse.CustomAccount(
                company.getRepresentativeId().getLastName(),
                company.getRepresentativeId().getFirstName(),
                company.getRepresentativeId().getEmail(),
                company.getRepresentativeId().getPhone()
        ));


        return getByCompanyResponse;
    }

    @Override
    public List<CompanyDTO>  getCompanyByAccountId() {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        Account account = accountRepository.findAccountByUserName(name)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        List<Company> company = companyRepository.findCompaniesByRepresentativeId_UserNameAndStatus(name, ECompanyStatus.ACTIVE);
        if(company.isEmpty()) {
            throw new AppException(ErrorCode.COMPANY_NOT_EXISTED);
        }
        return company.stream().map(c -> modelMapper.map(c, CompanyDTO.class)).collect(Collectors.toList());
    }

    @Override
    public List<GetByCompanyWithVerificationResponse> getCompanysByManager() {
        var context = SecurityContextHolder.getContext();
        String userName = context.getAuthentication().getName();

        Account account = accountRepository.findAccountByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (account.getRole().getRoleName() != ERole.MANAGER) {
            throw new AppException(ErrorCode.USER_NOT_MANAGER);
        }

        List<CompanyVerification> verifications = companyVerificationRepository.findCompanyVerificationsByAccount_UserName(userName);

        if (verifications.isEmpty()) {
            return Collections.emptyList();
        }

        return verifications.stream().map(verification -> {
            Company company = verification.getCompany();
            Account representative = company.getRepresentativeId();

            // Lấy danh sách CompanyDocuments và chuyển đổi sang DTO
            List<CompanyDocumentDTO> companyDocuments = companyDocumentRepository
                    .findCompanyDocumentsByCompany_CompanyId(company.getCompanyId())
                    .stream()
                    .map(doc -> modelMapper.map(doc, CompanyDocumentDTO.class))
                    .collect(Collectors.toList());

            GetByCompanyWithVerificationResponse.CustomAccount customAccount = (representative != null)
                    ? new GetByCompanyWithVerificationResponse.CustomAccount(
                    representative.getLastName(),
                    representative.getFirstName(),
                    representative.getEmail(),
                    representative.getPhone()
            )
                    : null;

            return new GetByCompanyWithVerificationResponse(
                    company.getCompanyId(),
                    company.getCompanyName(),
                    company.getCodeTax(),
                    company.getBankingName(),
                    company.getBankingCode(),
                    company.getNationalId(),
                    company.getLogoURL(),
                    company.getAddress(),
                    company.getDescription(),
                    companyDocuments, // Thêm danh sách tài liệu công ty
                    verification.getCompanyVerificationId(),
                    company.getStatus(),
                    customAccount
            );
        }).collect(Collectors.toList());
    }
    @Override
    public CompanyAndDocumentResponse createCompanyAndDocument(CreateCompanyRequest createCompanyRequest, MultipartFile logoURL, List<MultipartFile> companyDocument) throws IOException, MessagingException {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        var account = accountRepository.findAccountByUserName(name)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        String logoCompany = cloudinary.uploadImageToCloudinary(logoURL);

        // Create company
        Company company = new Company();
        company.setCompanyName(createCompanyRequest.getCompanyName());
        company.setDescription(createCompanyRequest.getDescription());
        company.setRepresentativeId(account);
        company.setCodeTax(createCompanyRequest.getCodeTax());
        company.setOwnerCard(createCompanyRequest.getOwnerCard());
        company.setEmail(createCompanyRequest.getEmail());
        company.setBankingCode(createCompanyRequest.getBankingCode());
        company.setBankingName(createCompanyRequest.getBankingName());
        company.setNationalId(createCompanyRequest.getNationalId());
        company.setLogoURL(logoCompany);
        company.setAddress(createCompanyRequest.getAddress());
        company.setStatus(ECompanyStatus.PENDING);
        companyRepository.save(company);

        var manager = accountRepository.findManagerWithLeastVerifications();
        if (manager == null) {
            throw new AppException(ErrorCode.MANAGER_NOT_FOUND);
        }

        // Create verification
        CreateCompanyVerificationRequest createCompanyVerificationRequest = new CreateCompanyVerificationRequest();
        createCompanyVerificationRequest.setCompanyId(company.getCompanyId());
        createCompanyVerificationRequest.setStatus(EVerificationStatus.PENDING);
        createCompanyVerificationRequest.setSubmitById(manager.get().getAccountId());

        var companyVerification = companyVerificationService.createCompanyVerification(createCompanyVerificationRequest);





        String notificationMessage = "Công ty mới cần duyệt: " + company.getCompanyName();

        messagingTemplate.convertAndSendToUser(manager.get().getUserName(), "/specific/messages", notificationMessage);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        int count = notificationRepository.countNotificationByAccountId(manager.get().getUserName());
        log.info("Count: {}", count);

        if(count >= 10) {
            Notification notification = notificationRepository.findTopByAccount_UserNameOrderByCreatedDateAsc(manager.get().getUserName())
                    .orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_NOT_EXISTED));
            notificationRepository.delete(notification);
        }
        Notification notification = new Notification();

        notification.setAccount(manager.get());
        notification.setMessage(notificationMessage);
        notification.setRead(false);
        notification.setCreatedDate(LocalDateTime.now());

        notificationRepository.save(notification);


        String fullname = (manager.get().getFirstName() != null ? manager.get().getFirstName() : "") +
                " " +
                (manager.get().getLastName() != null ? manager.get().getLastName() : "");
        fullname = fullname.trim();

        emailService.sendCompanyCreationRequestNotification(manager.get().getEmail(), company.getCompanyName(), fullname);

        // Create documents
        CreateCompanyDocumentRequest createCompanyDocumentRequest = new CreateCompanyDocumentRequest();
        createCompanyDocumentRequest.setCompanyId(company.getCompanyId());
        createCompanyDocumentRequest.setCompanyVerificationId(companyVerification.getCompanyVerificationId());

        createCompanyDocumentRequest.setUploadDate(LocalDateTime.now().format(formatter));

        List<CompanyDocumentDTO> companyDocumentDTOS = companyDocumentService.createCompanyDocument(createCompanyDocumentRequest, companyDocument);

        // Chuẩn bị response
        CreateCompanyResponse createCompanyResponse = new CreateCompanyResponse(
                company.getCompanyId(),
                company.getCompanyName(),
                company.getCodeTax(),
                company.getBankingName(),
                company.getBankingCode(),
                company.getOwnerCard(),
                company.getEmail(),
                company.getNationalId(),
                company.getLogoURL(),
                company.getAddress(),
                company.getDescription(),
                company.getStatus().name(),
                account.getAccountId(),
                companyVerification.getCompanyVerificationId()
        );

        return new CompanyAndDocumentResponse(createCompanyResponse, companyDocumentDTOS);
    }

    @Override
    public CompanyDTO isAccountHaveCompany() {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();
        var account = accountRepository.findAccountByUserName(name)
                .orElseThrow(()-> new AppException(ErrorCode.USER_NOT_EXISTED));

        var company = companyRepository.findCompanyByRepresentativeId_UserName(name);
        if(company.isEmpty()) {
            throw new AppException(ErrorCode.COMPANY_NOT_EXISTED);
        }
        return modelMapper.map(company.get(), CompanyDTO.class);
    }

    @Override
    public GetTransactionPaymenByCompanyIdResponse getTransactionPaymentContractByCompanyId(int companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new AppException(ErrorCode.COMPANY_NOT_FOUND));

        List<Contract> contractList = contractRepository.findContractsByCompany_CompanyId(companyId);

        if (contractList.isEmpty()) {
            return new GetTransactionPaymenByCompanyIdResponse(List.of());
        }

        List<ContractResponse> contractResponses = new ArrayList<>();

        for (Contract contract : contractList) {
            List<ContractDetail> contractDetails = contractDetailRepository.findContractDetailsByContract_ContractId(contract.getContractId());

            List<ContractDetailDTO> contractDetailDTOList = new ArrayList<>();
            for (ContractDetail detail : contractDetails) {
                ContractDetailDTO detailDTO = ContractDetailDTO.builder()
                        .contractDetailId(detail.getContractDetailId())
                        .contractDetailName(detail.getContractDetailName())
                        .contractDetailCode(detail.getContractDetailCode())
                        .description(detail.getDescription())
                        .contractAmount(detail.getAmount())
                        .contractPayDate(detail.getPayDate())
                        .status(String.valueOf(detail.getStatus()))
                        .contractId(detail.getContract().getContractId())
                        .build();
                contractDetailDTOList.add(detailDTO);
            }

            ContractResponse contractResponse = new ContractResponse(
                    contract.getContractId(),
                    contract.getContractName(),
                    contract.getTotalAmount(),
                    contract.getCommission(),
                    contract.getContractType(),
                    contract.getStartDate(),
                    contract.getEndDate(),
                    String.valueOf(contract.getStatus()),
                    contract.getAccount().getAccountId(),
                    contract.getEvent().getEventId(),
                    contract.getCompany().getCompanyId(),
                    contractDetailDTOList
            );

            contractResponses.add(contractResponse);
        }

        Collections.reverse(contractResponses);

        return new GetTransactionPaymenByCompanyIdResponse(contractResponses);
    }

    @Override
    public List<MyCompanyResponse> getCompanysByUserName(String userName) {
        List<Member> members = memberRepository.findMembersByAccount_UserName(userName);

        if (members.isEmpty()) {
            throw new AppException(ErrorCode.MEMBER_NOT_EXISTED);
        }

        List<MyCompanyResponse> companies = new ArrayList<>();
        for (Member member : members) {
            Company company = member.getCompany();
            if (company != null) {
                MyCompanyResponse myCompanyResponse = new MyCompanyResponse();
                myCompanyResponse.setCompanyId(company.getCompanyId());
                myCompanyResponse.setCompanyName(company.getCompanyName());
                myCompanyResponse.setLogoURL(company.getLogoURL());
                myCompanyResponse.setRepresentativeId(company.getRepresentativeId().getFirstName()+" "+company.getRepresentativeId().getLastName());
                myCompanyResponse.setAddress(company.getAddress());
                myCompanyResponse.setSubRole(member.getSubRole().name());

                companies.add(myCompanyResponse);
            }
        }

        if (companies.isEmpty()) {
            return Collections.emptyList();
        }

        return companies;
    }

    @Override
    public CompanyDTO getCompanyByEventId(int eventId) {
        var event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_EXISTED));

        var company = event.getCompany();

        if(company == null) {
            throw new AppException(ErrorCode.COMPANY_NOT_EXISTED);
        }
        return modelMapper.map(company, CompanyDTO.class);
    }

    @Override
    public CreateCompanyResponse updateCompany(int companyId, CreateCompanyRequest updateRequest, MultipartFile file, List<MultipartFile> fileDocument) throws IOException, MessagingException {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();

        Account account = accountRepository.findAccountByUserName(name)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        Company company = companyRepository.findCompanyByCompanyIdAndRepresentativeId_UserName(companyId, name)
                .orElseThrow(() -> new AppException(ErrorCode.COMPANY_NOT_EXISTED));
        if (company.getStatus() != ECompanyStatus.REJECTED) {
            throw new AppException(ErrorCode.COMPANY_NOT_REJECTED);
        }

        CompanyVerification companyVerification = companyVerificationRepository
                .findCompanyVerificationsByCompany_CompanyId(companyId)
                .orElseThrow(() -> new AppException(ErrorCode.COMPANY_VERIFICATION_NOT_EXISTED));

        // Upload và tạo document
        CreateCompanyDocumentRequest docRequest = new CreateCompanyDocumentRequest();
        docRequest.setCompanyId(company.getCompanyId());
        docRequest.setCompanyVerificationId(companyVerification.getCompanyVerificationId());
        docRequest.setUploadDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        companyDocumentService.createCompanyDocument(docRequest, fileDocument);

        // Upload logo mới nếu có
        if (file != null) {
            company.setLogoURL(cloudinary.uploadImageToCloudinary(file));
        }

        // Cập nhật các trường thông tin
        Optional.ofNullable(updateRequest.getCompanyName()).ifPresent(company::setCompanyName);
        Optional.ofNullable(updateRequest.getDescription()).ifPresent(company::setDescription);
        Optional.ofNullable(updateRequest.getCodeTax()).ifPresent(company::setCodeTax);
        Optional.ofNullable(updateRequest.getBankingCode()).ifPresent(company::setBankingCode);
        Optional.ofNullable(updateRequest.getBankingName()).ifPresent(company::setBankingName);
        Optional.ofNullable(updateRequest.getNationalId()).ifPresent(company::setNationalId);
        Optional.ofNullable(updateRequest.getEmail()).ifPresent(company::setEmail);
        Optional.ofNullable(updateRequest.getAddress()).ifPresent(company::setAddress);
        Optional.ofNullable(updateRequest.getOwnerCard()).ifPresent(company::setOwnerCard);

        // Cập nhật trạng thái công ty và xác minh
        company.setStatus(ECompanyStatus.PENDING);
        companyRepository.save(company);
        companyVerification.setStatus(EVerificationStatus.PENDING);
        companyVerificationRepository.save(companyVerification);

        // Gửi notification realtime
        String notifyMsg = "Công ty đã được cập nhật: " + company.getCompanyName();
        messagingTemplate.convertAndSendToUser(companyVerification.getAccount().getUserName(), "/specific/messages", notifyMsg);

        // Xử lý giới hạn 10 thông báo
        int count = notificationRepository.countNotificationByAccountId(companyVerification.getAccount().getUserName());
        if (count >= 10) {
            Notification oldest = notificationRepository.findTopByAccount_UserNameOrderByCreatedDateAsc(companyVerification.getAccount().getUserName())
                    .orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_NOT_EXISTED));
            notificationRepository.delete(oldest);
        }

        // Lưu notification mới
        Notification notification = new Notification();
        notification.setAccount(companyVerification.getAccount());
        notification.setMessage(notifyMsg);
        notification.setRead(false);
        notification.setCreatedDate(LocalDateTime.now());
        notificationRepository.save(notification);

        // Gửi email thông báo
        String fullname = (companyVerification.getAccount().getFirstName() != null ? companyVerification.getAccount().getFirstName() : "") +
                " " + (companyVerification.getAccount().getLastName() != null ? companyVerification.getAccount().getLastName() : "");
        emailService.sendCompanyCreationRequestNotification(
                companyVerification.getAccount().getEmail(), company.getCompanyName(), fullname.trim()
        );

        // Trả response
        return new CreateCompanyResponse(
                company.getCompanyId(),
                company.getCompanyName(),
                company.getCodeTax(),
                company.getBankingName(),
                company.getBankingCode(),
                company.getOwnerCard(),
                company.getEmail(),
                company.getNationalId(),
                company.getLogoURL(),
                company.getAddress(),
                company.getDescription(),
                company.getStatus().name(),
                account.getAccountId(),
                companyVerification.getCompanyVerificationId()
        );
    }



}
