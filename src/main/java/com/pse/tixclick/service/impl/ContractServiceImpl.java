package com.pse.tixclick.service.impl;

import com.pse.tixclick.email.EmailService;
import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.exception.ErrorCode;
import com.pse.tixclick.payload.dto.*;
import com.pse.tixclick.payload.entity.Account;
import com.pse.tixclick.payload.entity.company.Company;
import com.pse.tixclick.payload.entity.company.Contract;
import com.pse.tixclick.payload.entity.company.ContractDetail;
import com.pse.tixclick.payload.entity.company.ContractVerification;
import com.pse.tixclick.payload.entity.entity_enum.*;
import com.pse.tixclick.payload.entity.event.Event;
import com.pse.tixclick.payload.entity.event.EventActivity;
import com.pse.tixclick.payload.entity.payment.ContractPayment;
import com.pse.tixclick.payload.entity.seatmap.Seat;
import com.pse.tixclick.payload.entity.seatmap.SeatActivity;
import com.pse.tixclick.payload.entity.seatmap.Zone;
import com.pse.tixclick.payload.entity.seatmap.ZoneActivity;
import com.pse.tixclick.payload.entity.ticket.Ticket;
import com.pse.tixclick.payload.entity.ticket.TicketMapping;
import com.pse.tixclick.payload.request.create.ContractDetailRequest;
import com.pse.tixclick.payload.request.create.CreateContractAndDetailRequest;
import com.pse.tixclick.payload.request.create.CreateContractRequest;
import com.pse.tixclick.payload.response.ContractAndContractDetailResponse;
import com.pse.tixclick.repository.*;
import com.pse.tixclick.service.ContractDocumentService;
import com.pse.tixclick.service.ContractService;
import com.pse.tixclick.utils.AppUtils;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.modelmapper.ModelMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
    ContractVerificationRepository contractVerificationRepository;
    EventActivityRepository eventActivityRepository;
    ZoneActivityRepository zoneActivityRepository;
    SeatActivityRepository seatActivityRepository;
    ZoneRepository zoneRepository;
    SeatRepository seatRepository;
    EmailService emailService;
    TicketMappingRepository ticketMappingRepository;
    ContractDetailRepository contractDetailRepository;
    AccountRepository accountRepository1;
    SimpMessagingTemplate messagingTemplate;
    AppUtils appUtils;
    ContractPaymentRepository contractPaymentRepository;
    ContractDocumentService contractDocumentService;


    @Override
    public ContractDTO createContract(CreateContractRequest request) {
        var context = SecurityContextHolder.getContext();
        String userName = context.getAuthentication().getName();

        var manager = accountRepository.findAccountByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (manager.getRole().getRoleName() != ERole.MANAGER) {
            throw new AppException(ErrorCode.USER_NOT_MANAGER);
        }
        var contract = contractRepository.findById(request.getContractId())
                .orElseThrow(() -> new AppException(ErrorCode.CONTRACT_NOT_FOUND));

        var event = eventRepository.findEventByEventId(request.getEventId())
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        if (event.getStatus() != EEventStatus.PENDING_APPROVAL) {
            throw new AppException(ErrorCode.STATUS_NOT_CORRECT);
        }

        var company = companyRepository.findCompanyByCompanyId(event.getCompany().getCompanyId())
                .orElseThrow(() -> new AppException(ErrorCode.COMPANY_NOT_FOUND));



        // 🔹 Log để kiểm tra dữ liệu trước khi lưu
        System.out.println("Creating contract for eventId: " + event.getEventId());

        // 📝 Tạo và lưu hợp đồng

        contract.setContractName(request.getContractName());
        contract.setContractType(request.getContractType());
        contract.setStartDate(request.getStartDate());
        contract.setEndDate(request.getEndDate());
        contract.setStatus(EContractStatus.PENDING);
        contract.setEvent(event);
        contract.setAccount(manager);
        contract.setCompany(company);
        contract.setCommission(request.getCommission());
        contract.setTotalAmount(request.getTotalAmount());

        // 🔹 Lưu contract trước để có ID
        contractRepository.saveAndFlush(contract);

        // 📝 Tạo ContractVerification sau khi Contract đã có ID
        ContractVerification contractVerification = new ContractVerification();
        contractVerification.setContract(contract);
        contractVerification.setAccount(manager);
        contractVerification.setStatus(EVerificationStatus.PENDING);
        contractVerification.setVerifyDate(null);
        contractVerification.setNote("Awaiting verification");

        contractVerificationRepository.save(contractVerification);

        return modelMapper.map(contract, ContractDTO.class);
    }

    @Override
    public List<ContractAndDocumentsDTO> getAllContracts() {
        var context = SecurityContextHolder.getContext();
        String userName = context.getAuthentication().getName();
        var account = accountRepository.findAccountByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        if (account.getRole().getRoleName() != ERole.MANAGER) {
            throw new AppException(ErrorCode.USER_NOT_MANAGER);
        }
        List<Contract> contracts = contractRepository.findContractsByAccount_AccountId(account.getAccountId());

        if (contracts.isEmpty()) {
            return null;
        }

        List<ContractAndDocumentsDTO> contractDTOS = contracts.stream()
                .map(contract -> {
                    ContractAndDocumentsDTO contractAndDocumentsDTO = new ContractAndDocumentsDTO();
                    if (contract.getContractDocuments() != null) {
                        contractAndDocumentsDTO.setContractDocumentDTOS(
                                contract.getContractDocuments().stream()
                                        .map(contractDocument -> modelMapper.map(contractDocument, ContractDocumentDTO.class))
                                        .collect(Collectors.toList())
                        );
                    }

                    contractAndDocumentsDTO.setContractDTO(modelMapper.map(contract, ContractDTO.class));

                    return contractAndDocumentsDTO;
                })
                .collect(Collectors.toList());

        return contractDTOS.isEmpty() ? null : contractDTOS;
    }

    @Override
    public String approveContract(int contractId, EVerificationStatus status) throws MessagingException {
        var context = SecurityContextHolder.getContext();
        String userName = context.getAuthentication().getName();
        var account = accountRepository.findAccountByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new AppException(ErrorCode.CONTRACT_NOT_FOUND));
        Collection<ContractVerification> verifications = contract.getContractVerifications();

        if (verifications == null || verifications.isEmpty() || verifications.size() > 1) {
            throw new AppException(ErrorCode.CONTRACT_VERIFICATION_NOT_FOUND);
        }

// Lấy phần tử duy nhất từ collection
        ContractVerification verification = verifications.iterator().next();

// Tìm lại trong repository nếu cần thông tin đầy đủ
        ContractVerification contractVerification = contractVerificationRepository.findById(verification.getContractVerificationId())
                .orElseThrow(() -> new AppException(ErrorCode.CONTRACT_VERIFICATION_NOT_FOUND));



        // Kiểm tra xem user có phải là người xét duyệt hợp đồng hay không
        if (account.getAccountId() != contractVerification.getAccount().getAccountId()) {
            throw new AppException(ErrorCode.USER_NOT_MANAGER);
        }

        // Kiểm tra trạng thái hợp đồng (SỬA LỖI LOGIC)
        if (contractVerification.getStatus() != EVerificationStatus.PENDING) {
            throw new AppException(ErrorCode.CONTRACT_VERIFICATION_NOT_PENDING);
        }

        switch (status) {
            case APPROVED:
                String fullName = contractVerification.getContract().getCompany().getRepresentativeId().getFirstName() + " " +
                        contractVerification.getContract().getCompany().getRepresentativeId().getLastName();
                messagingTemplate.convertAndSendToUser(
                        contractVerification.getContract().getCompany().getRepresentativeId().getUserName(),
                        "/specific/messages",
                        "Your contract has been approved. Please check your email for details."
                );
                emailService.sendEventStartNotification(
                        contractVerification.getContract().getCompany().getRepresentativeId().getEmail(),
                        contractVerification.getContract().getEvent().getEventName(),
                        fullName
                );

                contractVerification.setStatus(EVerificationStatus.APPROVED);
                contractVerification.setNote("Contract approved");
                contractVerificationRepository.save(contractVerification);

                contract.setStatus(EContractStatus.APPROVED);
                contractRepository.save(contract);

                var event = contract.getEvent();
                var seatMap = event.getSeatMap();

                if (seatMap != null) {
                    // Lấy tất cả hoạt động (eventActivity) của event này
                    List<EventActivity> eventActivities = eventActivityRepository.findEventActivitiesByEvent_EventId(event.getEventId());

                    for (EventActivity eventActivity : eventActivities) {
                        // Gán seatMap cho eventActivity
                        eventActivity.setSeatMap(seatMap);
                        eventActivityRepository.save(eventActivity);

                        // Lấy danh sách zone từ seatMap
                        List<Zone> zones = zoneRepository.findBySeatMapId(seatMap.getSeatMapId());
                        for (Zone zone : zones) {
                            // Tạo ZoneActivity
                            ZoneActivity zoneActivity = new ZoneActivity();
                            zoneActivity.setZone(zone);
                            zoneActivity.setEventActivity(eventActivity);
                            zoneActivity.setAvailableQuantity(zone.getQuantity()); // Ban đầu, tất cả chỗ đều trống
                            zoneActivity = zoneActivityRepository.save(zoneActivity); // Lưu vào DB

                            // Nếu là Standing, bỏ qua SeatActivity
                            if (zone.getZoneType().getTypeName() == ZoneTypeEnum.STANDING) {
                                continue;
                            }

                            // Lấy danh sách ghế trong Zone
                            List<Seat> seats = seatRepository.findSeatsByZone_ZoneId(zone.getZoneId());
                            List<SeatActivity> seatActivities = new ArrayList<>();

                            for (Seat seat : seats) {
                                // Tạo SeatActivity
                                SeatActivity seatActivity = new SeatActivity();
                                seatActivity.setSeat(seat);
                                seatActivity.setZoneActivity(zoneActivity);
                                seatActivity.setEventActivity(eventActivity);
                                seatActivity.setStatus(ESeatActivityStatus.AVAILABLE); // Trạng thái mặc định
                                seatActivities.add(seatActivity);
                            }

                            // Lưu tất cả seatActivities một lần để giảm số lần truy cập DB
                            seatActivityRepository.saveAll(seatActivities);
                        }
                    }
                }else {
                    List<TicketMapping> ticketMappings = ticketMappingRepository.findTicketMappingsByEventActivity_Event(event);
                    if (ticketMappings.isEmpty()) {
                        throw new AppException(ErrorCode.EVENT_NOT_HAVE_SEATMAP);
                    }

                }


                event.setStatus(EEventStatus.SCHEDULED);

                return "Contract approved successfully";

            case REJECTED:
                contractVerification.setStatus(EVerificationStatus.REJECTED);
                contractVerificationRepository.save(contractVerification);
                return "Contract rejected successfully";

            default:
                contractVerification.setStatus(EVerificationStatus.PENDING);
                contractVerificationRepository.save(contractVerification);
                return "Contract status set to pending";
        }
    }

    @Override
    public CreateContractAndDetailRequest createContractAndContractDetail(MultipartFile file) throws IOException {
        String text = extractTextFromPdf(file);
        CreateContractAndDetailRequest request = parse(text);

        Event event = eventRepository.findEventByEventCode(request.getEventCode())
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        Account managerA = accountRepository.findAccountByEmail(request.getEmailA())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Company company = companyRepository.findCompanyByEmail(request.getEmailB())
                .orElseThrow(() -> new AppException(ErrorCode.COMPANY_NOT_FOUND));

        Contract contract = new Contract();

        // Cập nhật thông tin contract
        contract.setContractName(request.getContractName());
        contract.setTotalAmount(request.getContractValue());
        contract.setCommission(request.getCommission());
        contract.setContractType(request.getContractType());
        contract.setStartDate(request.getContractStartDate());
        contract.setEndDate(request.getContractEndDate());
        contract.setStatus(EContractStatus.PENDING); // Gán trạng thái đang duyệt
        contract.setEvent(event);
        contract.setAccount(managerA);
        contract.setCompany(company);
        contractRepository.save(contract);

        // Xử lý từng contract detail
        for (ContractDetailRequest dto : request.getContractDetails()) {
            ContractDetail contractDetail = new ContractDetail();
            contractDetail.setContract(contract);
            contractDetail.setContractDetailName(dto.getContractDetailName());
            contractDetail.setContractDetailCode(contractCodeAutomationCreating());
            contractDetail.setDescription(dto.getContractDetailDescription());
            contractDetail.setPercentage(dto.getContractDetailPercentage());
            contractDetail.setAmount(dto.getContractDetailAmount());
            contractDetail.setPayDate(dto.getContractDetailPayDate());
            contractDetail.setStatus(EContractDetailStatus.PENDING);

            contractDetailRepository.save(contractDetail);

            ContractPayment contractPayment = new ContractPayment();
            contractPayment.setPaymentAmount(dto.getContractDetailAmount());
            contractPayment.setContractDetail(contractDetail);
            contractPayment.setNote(dto.getContractDetailDescription());
            contractPayment.setPaymentMethod("Thanh Toan Ngan Hang");
            contractPayment.setStatus(EContractPaymentStatus.PENDING);
            contractPaymentRepository.save(contractPayment);
        }
        contractDocumentService.uploadContractDocument(file, contract.getContractId());

        return request;

    }

    public CreateContractAndDetailRequest parse(String text) {
        CreateContractAndDetailRequest dto = new CreateContractAndDetailRequest();

        // Các trường trong CreateContractAndDetailRequest
        dto.setContractName(extract(text, "HỢP ĐỒNG CUNG ỨNG DỊCH VỤ TỔ CHỨC SỰ KIỆN"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        dto.setContractStartDate(LocalDate.parse(extract(text, "kí: (\\d{2}/\\d{2}/\\d{4})"), formatter));
        dto.setContractEndDate(LocalDate.parse(extract(text, "đến (\\d{2}/\\d{2}/\\d{4})"), formatter));

        dto.setRepresentativeA(
                extract(text, "BÊN A[\\s\\S]*?Đại diện bởi\\s*:\\s*(.*?)\\s*Chức vụ")
        );
        dto.setEmailA(
                extract(text, "BÊN A[\\s\\S]*?Email\\s*[:：]\\s*(\\S+@\\S+)")  // Cập nhật biểu thức regex cho email A
        );
        dto.setRepresentativeB(
                extract(text, "BÊN B[\\s\\S]*?Đại diện bởi\\s*:\\s*(.*?)\\s*Chức vụ")
        );
        dto.setEmailB(
                extract(text, "BÊN B[\\s\\S]*?Email\\s*[:：]\\s*(\\S+@\\S+)")  // Cập nhật biểu thức regex cho email B
        );
        String contractValueStr = extract(text, "Tổng giá trị hợp đồng\\s*:\\s*([\\d\\.]+) VND");

// Kiểm tra và xử lý chuỗi trước khi chuyển đổi
        contractValueStr = contractValueStr.replace(".", ""); // Loại bỏ dấu chấm phân cách phần nghìn
        contractValueStr = contractValueStr.replace(",", "."); // Thay dấu phẩy thành dấu chấm

// Chuyển đổi thành Double
        dto.setContractValue(Double.valueOf(contractValueStr));

        dto.setCommission(extract(text, "hoa hồng.*?([\\d\\.]+)%"));
        dto.setEventCode(extract(text, "Mã sự kiện:\\s*(\\S+)"));

        String type = extract(text, "Trường hợp thanh toán 1 lần");
        if (type != null && type.contains("Trường hợp thanh toán 1 lần")) {
            dto.setContractType(EContractType.ONE_TIME.name());
            List<ContractDetailRequest> contractDetails = new ArrayList<>();
            contractDetails.add(parseContractDetailOneTime(text));
            dto.setContractDetails(contractDetails);
        } else {
            dto.setContractType(EContractType.INSTALLMENT.name());
            dto.setContractDetails(parseContractDetailInstallment(text));
        }
        return dto;
    }

    private String extract(String text, String regex) {
        if (text == null || regex == null || text.isEmpty()) {
            return null;
        }

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            // Ensure there's at least one capturing group
            if (matcher.groupCount() > 0) {
                return matcher.group(1).trim();
            } else {
                // Return the matched string directly if no group is defined
                return matcher.group().trim();
            }
        }

        return null;
    }

    private ContractDetailRequest parseContractDetailOneTime(String text) {
        // Cải tiến biểu thức chính quy để chỉ khớp với phần trăm trong phần thanh toán một lần
        Pattern p = Pattern.compile("Bên A sẽ thanh toán một lần cho Bên B (\\d{1,3})% giá trị của hợp đồng", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(text);

        if (m.find()) {
            ContractDetailRequest pmt = new ContractDetailRequest();
            pmt.setContractDetailName("Thanh toán một lần");

            // Lấy phần trăm thanh toán (phần trăm từ "Bên A sẽ thanh toán một lần")
            String percentageStr = m.group(1).trim();
            if (!percentageStr.isEmpty()) {
                pmt.setContractDetailPercentage(Double.parseDouble(percentageStr));
            }

            // Tiền thanh toán: "2.250.000.000" => 2250000000
            Pattern amountPattern = Pattern.compile("tương đương\\s*([\\d\\.]+)", Pattern.CASE_INSENSITIVE);
            Matcher amountMatcher = amountPattern.matcher(text);
            if (amountMatcher.find()) {
                String amountStr = amountMatcher.group(1).replace(".", "");
                pmt.setContractDetailAmount(Double.parseDouble(amountStr));
            }

            // Lấy ngày thanh toán: ngày sau "vào ngày"
            Pattern datePattern = Pattern.compile("vào ngày\\s*(\\d{2}/\\d{2}/\\d{4})", Pattern.CASE_INSENSITIVE);
            Matcher dateMatcher = datePattern.matcher(text);
            if (dateMatcher.find()) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                pmt.setContractDetailPayDate(LocalDate.parse(dateMatcher.group(1), formatter));
            }

            pmt.setContractDetailCode(contractCodeAutomationCreating());
            // Ghi chú mô tả
            pmt.setContractDetailDescription("Theo nội dung hợp đồng thanh toán một lần");

            return pmt;
        }

        return null;
    }






    private List<ContractDetailRequest> parseContractDetailInstallment(String text) {
        List<ContractDetailRequest> list = new ArrayList<>();

        Pattern p = Pattern.compile("Lần (\\d+):.*?([\\d,\\.]+)%.*?([\\d\\.]+) VNĐ.*?ngày (\\d{2}/\\d{2}/\\d{4})", Pattern.DOTALL);
        Matcher m = p.matcher(text);

        while (m.find()) {
            ContractDetailRequest pmt = new ContractDetailRequest();
            pmt.setContractDetailName("Lần" + String.valueOf(Integer.parseInt(m.group(1))));
            pmt.setContractDetailPercentage(Double.parseDouble(m.group(2).replace(",", "")));
            pmt.setContractDetailCode(contractCodeAutomationCreating());
            pmt.setContractDetailAmount(Double.parseDouble(m.group(3).replace(".", "")));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            pmt.setContractDetailPayDate(LocalDate.parse(m.group(4), formatter));
            pmt.setContractDetailDescription("Theo nội dung hợp đồng thanh toán nhiều lần");
            list.add(pmt);
        }

        return list;
    }

    public String extractTextFromPdf(MultipartFile file) throws IOException {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private String contractCodeAutomationCreating() {
        Account account = appUtils.getAccountFromAuthentication();
        int accountId = account.getAccountId(); // Giả định bạn có hàm lấy userId

        // Lấy thời gian hiện tại
        String date = new SimpleDateFormat("yyyyMMdd").format(new Date());

        // Tạo phần số thứ tự tự động hoặc ngẫu nhiên cho mã đơn hàng
        String uniqueId = String.format("%04d", new Random().nextInt(10000));

        return accountId + date  + uniqueId;
    }





}
