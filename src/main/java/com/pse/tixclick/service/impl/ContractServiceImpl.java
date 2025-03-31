package com.pse.tixclick.service.impl;

import com.pse.tixclick.email.EmailService;
import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.exception.ErrorCode;
import com.pse.tixclick.payload.dto.ContractDTO;
import com.pse.tixclick.payload.dto.PaymentDTO;
import com.pse.tixclick.payload.entity.Account;
import com.pse.tixclick.payload.entity.company.Contract;
import com.pse.tixclick.payload.entity.company.ContractVerification;
import com.pse.tixclick.payload.entity.entity_enum.*;
import com.pse.tixclick.payload.entity.event.EventActivity;
import com.pse.tixclick.payload.entity.seatmap.Seat;
import com.pse.tixclick.payload.entity.seatmap.SeatActivity;
import com.pse.tixclick.payload.entity.seatmap.Zone;
import com.pse.tixclick.payload.entity.seatmap.ZoneActivity;
import com.pse.tixclick.payload.request.create.CreateContractRequest;
import com.pse.tixclick.payload.response.QRCompanyResponse;
import com.pse.tixclick.repository.*;
import com.pse.tixclick.service.ContractService;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Email;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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

    @Override
    public ContractDTO createContract(CreateContractRequest request) {
        var context = SecurityContextHolder.getContext();
        String userName = context.getAuthentication().getName();

        var manager = accountRepository.findAccountByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (manager.getRole().getRoleName() != ERole.MANAGER) {
            throw new AppException(ErrorCode.USER_NOT_MANAGER);
        }

        var event = eventRepository.findEventByEventId(request.getEventId())
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        if (event.getStatus() != EEventStatus.PENDING) {
            throw new AppException(ErrorCode.STATUS_NOT_CORRECT);
        }

        var company = companyRepository.findCompanyByCompanyId(event.getCompany().getCompanyId())
                .orElseThrow(() -> new AppException(ErrorCode.COMPANY_NOT_FOUND));

        // 🔹 Kiểm tra xem đã có hợp đồng cho event này chưa
        if (contractRepository.existsByEvent(event)) {
            throw new AppException(ErrorCode.CONTRACT_ALREADY_EXISTS);
        }

        // 🔹 Log để kiểm tra dữ liệu trước khi lưu
        System.out.println("Creating contract for eventId: " + event.getEventId());

        // 📝 Tạo và lưu hợp đồng
        Contract newContract = new Contract();
        newContract.setContractName(request.getContractName());
        newContract.setContractType(request.getContractType());
        newContract.setEvent(event);
        newContract.setAccount(manager);
        newContract.setCompany(company);
        newContract.setCommission(request.getCommission());
        newContract.setTotalAmount(request.getTotalAmount());

        // 🔹 Lưu contract trước để có ID
        contractRepository.saveAndFlush(newContract);

        // 📝 Tạo ContractVerification sau khi Contract đã có ID
        ContractVerification contractVerification = new ContractVerification();
        contractVerification.setContract(newContract);
        contractVerification.setAccount(manager);
        contractVerification.setStatus(EVerificationStatus.PENDING);
        contractVerification.setVerifyDate(null);
        contractVerification.setNote("Awaiting verification");

        contractVerificationRepository.save(contractVerification);

        return modelMapper.map(newContract, ContractDTO.class);
    }



    @Override
    public List<ContractDTO> getAllContracts() {
        List<Contract> contracts = contractRepository.findAll();
        return contracts.stream()
                .map(contract -> modelMapper.map(contract, ContractDTO.class))
                .toList();
    }

    @Override
    public String approveContract(int contractVerificationId, EVerificationStatus status) throws MessagingException {
        var context = SecurityContextHolder.getContext();
        String userName = context.getAuthentication().getName();
        var account = accountRepository.findAccountByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        ContractVerification contractVerification = contractVerificationRepository.findById(contractVerificationId)
                .orElseThrow(() -> new AppException(ErrorCode.CONTRACT_VERIFICATION_NOT_FOUND));

        // Kiểm tra xem user có phải là người xét duyệt hợp đồng hay không
        if (account.getAccountId() != contractVerification.getAccount().getAccountId()) {
            throw new AppException(ErrorCode.USER_NOT_MANAGER);
        }

        // Kiểm tra trạng thái hợp đồng (SỬA LỖI LOGIC)
        if (contractVerification.getStatus() != EVerificationStatus.PENDING
                && contractVerification.getStatus() != EVerificationStatus.REVIEWING) {
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
                contractVerificationRepository.save(contractVerification);

                var contract = contractVerification.getContract();
                var event = contract.getEvent();
                var seatMap = event.getSeatMap();

                if (seatMap == null) {
                    throw new AppException(ErrorCode.SEAT_MAP_NOT_FOUND);
                }

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



}
