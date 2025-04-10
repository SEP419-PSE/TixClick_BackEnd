package com.pse.tixclick.service.impl;

import com.pse.tixclick.email.EmailService;
import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.exception.ErrorCode;
import com.pse.tixclick.payload.dto.*;
import com.pse.tixclick.payload.entity.company.Contract;
import com.pse.tixclick.payload.entity.company.ContractDetail;
import com.pse.tixclick.payload.entity.company.ContractVerification;
import com.pse.tixclick.payload.entity.entity_enum.*;
import com.pse.tixclick.payload.entity.event.EventActivity;
import com.pse.tixclick.payload.entity.seatmap.Seat;
import com.pse.tixclick.payload.entity.seatmap.SeatActivity;
import com.pse.tixclick.payload.entity.seatmap.Zone;
import com.pse.tixclick.payload.entity.seatmap.ZoneActivity;
import com.pse.tixclick.payload.entity.ticket.Ticket;
import com.pse.tixclick.payload.entity.ticket.TicketMapping;
import com.pse.tixclick.payload.request.create.CreateContractRequest;
import com.pse.tixclick.payload.response.ContractAndContractDetailResponse;
import com.pse.tixclick.repository.*;
import com.pse.tixclick.service.ContractService;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
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
        List<Contract> contracts = contractRepository.findAll();

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

                emailService.sendEventStartNotification(
                        contractVerification.getContract().getCompany().getRepresentativeId().getEmail(),
                        contractVerification.getContract().getEvent().getEventName(),
                        fullName
                );

                contractVerification.setStatus(EVerificationStatus.APPROVED);
                contractVerification.setNote("Contract approved");
                contractVerificationRepository.save(contractVerification);

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
                                seatActivity.setStatus(String.valueOf(ESeatActivityStatus.AVAILABLE)); // Trạng thái mặc định
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
    public ContractAndContractDetailResponse createContractAndContractDetail(ContractAndContractDetailResponse request) {
        // Lấy contract theo ID
        Contract contract = contractRepository.findById(request.getContractId())
                .orElseThrow(() -> new AppException(ErrorCode.CONTRACT_NOT_FOUND));

        // Cập nhật thông tin contract
        contract.setContractName(request.getContractName());
        contract.setTotalAmount(request.getTotalAmount());
        contract.setCommission(request.getCommission());
        contract.setContractType(request.getContractType());
        contract.setStartDate(request.getStartDate());
        contract.setEndDate(request.getEndDate());
        contract.setStatus(EContractStatus.PENDING); // Gán trạng thái đang duyệt
        contractRepository.save(contract);

        // Xử lý từng contract detail
        for (ContractDetailDTO dto : request.getContractDetailDTOS()) {
            ContractDetail contractDetail = new ContractDetail();
            contractDetail.setContract(contract);
            contractDetail.setContractDetailName(dto.getContractDetailName());
            contractDetail.setContractDetailCode(dto.getContractDetailCode());
            contractDetail.setDescription(dto.getDescription());
            contractDetail.setAmount(dto.getContractAmount());
            contractDetail.setPayDate(dto.getContractPayDate());
            contractDetail.setStatus(dto.getStatus().toUpperCase()); // Enum safe

            contractDetailRepository.save(contractDetail);
        }
        ContractVerification contractVerification = new ContractVerification();
        contractVerification.setContract(contract);
        contractVerification.setAccount(contract.getAccount());
        contractVerification.setStatus(EVerificationStatus.PENDING);
        contractVerification.setVerifyDate(null);
        contractVerification.setNote("Awaiting verification");
        contractVerificationRepository.save(contractVerification);
        return request;
    }



}
