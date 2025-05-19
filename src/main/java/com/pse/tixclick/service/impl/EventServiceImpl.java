package com.pse.tixclick.service.impl;

import com.pse.tixclick.cloudinary.CloudinaryService;
import com.pse.tixclick.email.EmailService;
import com.pse.tixclick.payload.dto.*;
import com.pse.tixclick.payload.entity.Account;
import com.pse.tixclick.payload.entity.CheckinLog;
import com.pse.tixclick.payload.entity.Notification;
import com.pse.tixclick.payload.entity.company.Company;
import com.pse.tixclick.payload.entity.company.CompanyVerification;
import com.pse.tixclick.payload.entity.company.Contract;
import com.pse.tixclick.payload.entity.entity_enum.*;
import com.pse.tixclick.payload.entity.event.EventActivity;
import com.pse.tixclick.payload.entity.seatmap.Seat;
import com.pse.tixclick.payload.entity.ticket.Ticket;
import com.pse.tixclick.payload.entity.ticket.TicketMapping;
import com.pse.tixclick.payload.entity.ticket.TicketPurchase;
import com.pse.tixclick.payload.response.*;
import com.pse.tixclick.repository.*;
import com.pse.tixclick.service.TicketMappingService;
import com.pse.tixclick.utils.AppUtils;
import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.exception.ErrorCode;
import com.pse.tixclick.payload.entity.event.Event;
import com.pse.tixclick.payload.request.create.CreateEventRequest;
import com.pse.tixclick.payload.request.update.UpdateEventRequest;
import com.pse.tixclick.service.EventService;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
@Slf4j
public class EventServiceImpl implements EventService {
    EventRepository eventRepository;
    ModelMapper modelMapper;
    EventCategoryRepository eventCategoryRepository;
    AccountRepository accountRepository;
    CloudinaryService cloudinary;
    CompanyRepository companyRepository;
    SimpMessagingTemplate messagingTemplate;
    ContractRepository contractRepository;
    EmailService emailService;
    TicketRepository ticketRepository;
    SeatMapRepository seatMapRepository;
    TicketMappingRepository ticketMappingRepository;
    TicketPurchaseRepository ticketPurchaseRepository;
    AppUtils appUtils;
    OrderRepository orderRepository;
    TicketMappingService ticketMappingService;
    NotificationRepository notificationRepository;
    OrderDetailRepository orderDetailRepository;
    EventActivityRepository eventActivityRepository;
    CheckinLogRepository checkinLogRepository;
    CompanyVerificationRepository companyVerificationRepository;
    SimpMessagingTemplate simpMessagingTemplate;

    @Override
    public EventDTO createEvent(CreateEventRequest request, MultipartFile logoURL, MultipartFile bannerURL) throws IOException {
        AppUtils.checkRole(ERole.ORGANIZER, ERole.BUYER);
        if (request == null || request.getEventName() == null || request.getCategoryId() == 0) {
            throw new AppException(ErrorCode.INVALID_EVENT_DATA);
        }


        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();
        var organnizer = accountRepository.findAccountByUserName(name)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        var category = eventCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        var company = companyRepository.findCompanyByCompanyIdAndRepresentativeId_UserName(request.getCompanyId(), name)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_CREATE_COMPANY));
        if (company.getStatus() != ECompanyStatus.ACTIVE) {
            throw new AppException(ErrorCode.COMPANY_NOT_ACTIVE);
        } else if (company.getRepresentativeId().getAccountId() != organnizer.getAccountId()) {
            throw new AppException(ErrorCode.INVALID_COMPANY);
        }
        // Upload từng ảnh lên Cloudinary
        String logocode = cloudinary.uploadImageToCloudinary(logoURL);
        String bannercode = cloudinary.uploadImageToCloudinary(bannerURL);
        // Tạo đối tượng Event từ request
        Event event = new Event();

        // Tạo eventCode từ tên sự kiện và mã danh mục, có thể kết hợp thêm các phần tử khác như ngày tháng để đảm bảo tính duy nhất
        String eventCode = category.getCategoryName().toUpperCase() + System.currentTimeMillis();
        event.setEventCode(eventCode);  // Gán eventCode cho sự kiện

        CompanyVerification companyVerification = companyVerificationRepository.findCompanyVerificationsByCompany_CompanyId(company.getCompanyId())
                .orElseThrow(() -> new AppException(ErrorCode.COMPANY_VERIFICATION_NOT_FOUND));
        event.setManagerId(companyVerification.getAccount().getAccountId());
        event.setEventName(request.getEventName());
        String typeEvent = request.getTypeEvent().toUpperCase();

        // Kiểm tra xem typeEvent có phải là "ONLINE" hoặc "OFFLINE" không (có thể sử dụng toUpperCase() để kiểm tra không phân biệt chữ hoa chữ thường)
        if ("ONLINE".equalsIgnoreCase(typeEvent) || "OFFLINE".equalsIgnoreCase(typeEvent)) {
            // Chuyển typeEvent về chữ in hoa trước khi gán vào event
            event.setTypeEvent(typeEvent.toUpperCase());
        } else {
            // Nếu không phải ONLINE hoặc OFFLINE, có thể xử lý thêm tùy theo yêu cầu
            // Ví dụ: throw exception, log error hoặc gán một giá trị mặc định
            throw new IllegalArgumentException("Invalid event type. Must be ONLINE or OFFLINE.");
        }
        event.setDescription(request.getDescription());
        event.setCategory(category);

        event.setStatus(EEventStatus.DRAFT);
        event.setLogoURL(logocode);
        event.setBannerURL(bannercode);
        event.setOrganizer(organnizer);
        event.setCountView(0);
        event.setCompany(company);
        if ("ONLINE".equals(request.getTypeEvent().toUpperCase())) {
            event.setUrlOnline(request.getUrlOnline());
            event.setLocationName(null);
            event.setAddress(null);
            event.setWard(null);
            event.setDistrict(null);
            event.setCity(null);
        } else if ("OFFLINE".equals(request.getTypeEvent().toUpperCase())) {
            event.setUrlOnline(null);
            event.setLocationName(request.getLocationName());
            event.setAddress(request.getAddress());
            event.setWard(request.getWard());
            event.setDistrict(request.getDistrict());
            event.setCity(request.getCity());
        }


        // Lưu vào database
        event = eventRepository.save(event);


        // Chuyển đổi sang DTO để trả về
        return modelMapper.map(event, EventDTO.class);
    }

    @Override
    public EventDTO updateEvent(UpdateEventRequest eventRequest, MultipartFile logoURL, MultipartFile bannerURL) throws IOException {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        // Lấy sự kiện từ database
        var event = eventRepository.findEventByEventIdAndOrganizer_UserName(eventRequest.getEventId(), name)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        // Kiểm tra trạng thái sự kiện, chỉ cho phép cập nhật nếu sự kiện ở trạng thái DRAFT
        if (event.getStatus() != EEventStatus.DRAFT) {
            throw new AppException(ErrorCode.INVALID_EVENT_STATUS);
        }

        // Chỉ cập nhật nếu giá trị không null hoặc không phải chuỗi trống
        if (eventRequest.getEventName() != null && !eventRequest.getEventName().trim().isEmpty()) {
            event.setEventName(eventRequest.getEventName());
        }

        if (eventRequest.getDescription() != null && !eventRequest.getDescription().trim().isEmpty()) {
            event.setDescription(eventRequest.getDescription());
        }

        // Cập nhật trạng thái sự kiện nếu có
        if (eventRequest.getStatus() != null) {
            event.setStatus(EEventStatus.valueOf(eventRequest.getStatus()));
        }

        // Cập nhật URL online nếu sự kiện là ONLINE
        if (eventRequest.getUrlOnline() != null && !eventRequest.getUrlOnline().trim().isEmpty()) {
            if ("ONLINE".equalsIgnoreCase(event.getTypeEvent())) {
                event.setUrlOnline(eventRequest.getUrlOnline());
                event.setAddress(null);
                event.setWard(null);  // Xóa các địa chỉ khi là sự kiện ONLINE
                event.setDistrict(null);
                event.setCity(null);
                event.setLocationName(null);
            } else {
                System.out.println("Event type is not ONLINE, skip setting urlOnline.");
            }
        }

        // Cập nhật loại sự kiện
        if (eventRequest.getTypeEvent() != null) {
            String type = eventRequest.getTypeEvent().toUpperCase();
            if (!type.equals("ONLINE") && !type.equals("OFFLINE")) {
                throw new AppException(ErrorCode.INVALID_EVENT_TYPE);
            }
            event.setTypeEvent(type);
        }

        // Cập nhật danh mục sự kiện nếu có
        if (eventRequest.getCategoryId() != 0) {
            var category = eventCategoryRepository.findById(eventRequest.getCategoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
            event.setCategory(category);
        }

        // Cập nhật thông tin địa chỉ nếu sự kiện là OFFLINE
        if ("OFFLINE".equals(event.getTypeEvent())) {
            event.setUrlOnline(null);  // Đảm bảo rằng không có URL online cho sự kiện OFFLINE
            if (eventRequest.getLocationName() != null && !eventRequest.getLocationName().trim().isEmpty()) {
                event.setLocationName(eventRequest.getLocationName());
            }
            if (eventRequest.getWard() != null && !eventRequest.getWard().trim().isEmpty()) {
                event.setWard(eventRequest.getWard());
            }
            if (eventRequest.getDistrict() != null && !eventRequest.getDistrict().trim().isEmpty()) {
                event.setDistrict(eventRequest.getDistrict());
            }
            if (eventRequest.getCity() != null && !eventRequest.getCity().trim().isEmpty()) {
                event.setCity(eventRequest.getCity());
            }
            if (eventRequest.getAddress() != null && !eventRequest.getAddress().trim().isEmpty()) {
                event.setAddress(eventRequest.getAddress());
            }
        }

        // Xử lý upload file nếu có
        if (logoURL != null && !logoURL.isEmpty()) {
            String logoUrl = cloudinary.uploadImageToCloudinary(logoURL);
            event.setLogoURL(logoUrl);
        }

        if (bannerURL != null && !bannerURL.isEmpty()) {
            String bannerUrl = cloudinary.uploadImageToCloudinary(bannerURL);
            event.setBannerURL(bannerUrl);
        }

        // Lưu thay đổi vào database
        event = eventRepository.save(event);

        // Chuyển đổi sang DTO và trả về
        return modelMapper.map(event, EventDTO.class);
    }


    @Override
    public boolean deleteEvent(int id) {
        var event = eventRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
        event.setStatus(EEventStatus.REJECTED);
        return true;
    }

    @Override
    public List<EventResponse> getAllEvent() {
        List<Event> events = eventRepository.findAll();
        return events.stream().map(event -> {
            EventResponse response = new EventResponse();
            response.setEventId(event.getEventId());
            response.setBannerURL(event.getBannerURL());
            response.setLogoURL(event.getLogoURL());
            response.setDescription(event.getDescription());
            response.setLocationName(event.getLocationName());
            response.setEventName(event.getEventName());
            response.setEventCode(event.getEventCode());
            response.setCity(event.getCity());
            response.setDistrict(event.getDistrict());
            response.setWard(event.getWard());
            response.setStatus(String.valueOf(event.getStatus()));
            response.setTypeEvent(event.getTypeEvent());

            if (event.getOrganizer() != null) {
                response.setOrganizerId(event.getOrganizer().getAccountId());
                response.setOrganizerName(event.getOrganizer().getUserName());
            }

            if (event.getCompany() != null) {
                response.setCompanyId(event.getCompany().getCompanyId());
                response.setCompanyName(event.getCompany().getCompanyName());
            }

            return response;
        }).collect(Collectors.toList());
    }

    @Override
    public List<EventResponse> getAllEventScheduledAndPendingApproved() {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();
        Account account = accountRepository.findAccountByUserName(name)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        List<Event> events = eventRepository.findEventsByManagerId(account.getAccountId());

        if (events.isEmpty()) {
            return Collections.emptyList();
        }
        return events.stream()
                .filter(event -> event.getStatus() != EEventStatus.DRAFT )
                .map(event -> {
                    EventResponse response = new EventResponse();
                    response.setEventId(event.getEventId());
                    response.setBannerURL(event.getBannerURL());
                    response.setLogoURL(event.getLogoURL());
                    response.setDescription(event.getDescription());
                    response.setLocationName(event.getLocationName());
                    response.setEventName(event.getEventName());
                    response.setEventCode(event.getEventCode());
                    response.setCity(event.getCity());
                    response.setDistrict(event.getDistrict());
                    response.setWard(event.getWard());
                    response.setAddress(event.getAddress());
                    response.setStatus(String.valueOf(event.getStatus()));
                    response.setTypeEvent(event.getTypeEvent());
                    List<Contract> contracts = contractRepository.findContractByEventId(event.getEventId());

                    if (contracts != null && !contracts.isEmpty()) {
                        Contract contract = contracts.get(0);  // Lấy hợp đồng đầu tiên
                        // Gán giá trị mặc định là chuỗi rỗng nếu contractCode là null
                        response.setContractCode(contract.getContractCode() != null ? contract.getContractCode() : "");
                    } else {
                        response.setContractCode(null);  // Nếu không có contract, gán giá trị null hoặc chuỗi rỗng
                    }



                    if (event.getOrganizer() != null) {
                        response.setOrganizerId(event.getOrganizer().getAccountId());
                        response.setOrganizerName(event.getOrganizer().getUserName());
                    }

                    if (event.getCompany() != null) {
                        response.setCompanyId(event.getCompany().getCompanyId());
                        response.setCompanyName(event.getCompany().getCompanyName());
                    }

                    return response;
                }).collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        list -> {
                            Collections.reverse(list);
                            return list;
                        }
                ));

    }


    @Override
    public EventDTO getEventById(int id) {
        var event = eventRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
        return modelMapper.map(event, EventDTO.class);
    }

    @Override
    public List<EventDTO> getEventByStatus(EEventStatus status) {
        List<Event> events = eventRepository.findEventsByStatus(status);
        if (events.isEmpty()) {
            throw new AppException(ErrorCode.EVENT_NOT_FOUND);
        }

        return modelMapper.map(events, new TypeToken<List<EventDTO>>() {
        }.getType());
    }

    @Override
    public List<EventDTO> getEventByDraft() {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();
        var account = accountRepository.findAccountByUserName(name)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        List<Event> events = eventRepository.findEventsByStatusAndOrganizer_UserName("DRAFT", name)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
        return modelMapper.map(events, new TypeToken<List<EventDTO>>() {
        }.getType());
    }

    @Override
    public List<EventDTO> getEventByCompleted() {
        List<Event> events = eventRepository.findEventsByStatus(EEventStatus.valueOf("COMPLETED"));
        if (events.isEmpty()) {
            throw new AppException(ErrorCode.EVENT_NOT_FOUND);
        }
        return modelMapper.map(events, new TypeToken<List<EventDTO>>() {
        }.getType());
    }


    @Override
    public List<EventDTO> getAllEventsByAccountId() {
        int uId = appUtils.getAccountFromAuthentication().getAccountId();
        List<Event> events = eventRepository.findEventByOrganizerId(uId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
        return modelMapper.map(events, new TypeToken<List<EventDTO>>() {
        }.getType());
    }

    @Override
    public List<EventDTO> getEventsByAccountIdAndStatus(String status) {
        int uId = appUtils.getAccountFromAuthentication().getAccountId();
        List<Event> events = eventRepository.findEventByOrganizerIdAndStatus(uId, EEventStatus.valueOf(status))
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
        return modelMapper.map(events, new TypeToken<List<EventDTO>>() {
        }.getType());
    }

    @Override
    public List<EventDTO> getEventsByCompanyId(int companyId) {


        List<Event> events = eventRepository.findEventsByCompany_CompanyId(companyId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
        return modelMapper.map(events, new TypeToken<List<EventDTO>>() {
        }.getType());

    }

    @Override
    public int countTotalScheduledEvents() {
        return Optional.of(eventRepository.countTotalScheduledEvents()).orElse(0);
    }

    @Override
    public double getAverageTicketPrice() {
        Double sum = eventRepository.getAverageTicketPrice();
        return sum == null ? 0 : sum;
    }

    @Override
    public Map<String, Double> getEventCategoryDistribution() {
        List<Object[]> results = eventRepository.getEventCategoryDistribution();
        Map<String, Double> distributionMap = new HashMap<>();

        for (Object[] result : results) {
            distributionMap.put((String) result[0], ((Number) result[1]).doubleValue());
        }
        return distributionMap;
    }

    @Override
    public List<UpcomingEventDTO> getUpcomingEvents() {
        List<Event> events = eventRepository.findScheduledEvents();
        List<UpcomingEventDTO> upcomingEventDTOs = new ArrayList<>();

        for (Event event : events) {
            int eventId = event.getEventId();

            int totalTicketsSold = ticketPurchaseRepository.countTotalTicketSold(eventId);

            double totalRevenue = orderRepository.sumTotalTransaction(eventId);

            UpcomingEventDTO dto = new UpcomingEventDTO();
            dto.setEventName(event.getEventName());
            dto.setTicketSold(totalTicketsSold);
            dto.setRevenue(totalRevenue);

            upcomingEventDTOs.add(dto);
        }

        Collections.reverse(upcomingEventDTOs); // Đảo ngược thứ tự

        return upcomingEventDTOs;
    }


    @Override
    public List<UpcomingEventDTO> getTopPerformingEvents() {

        List<Event> events = eventRepository.findScheduledEvents();
        List<UpcomingEventDTO> upcomingEventDTOs = new ArrayList<>();

        for (Event event : events) {
            int eventId = event.getEventId();

            int totalTicketsSold = ticketPurchaseRepository.countTotalTicketSold(eventId);

            double totalRevenue = orderRepository.sumTotalTransaction(eventId);

            UpcomingEventDTO dto = new UpcomingEventDTO();
            dto.setEventName(event.getEventName());
            dto.setTicketSold(totalTicketsSold);
            dto.setRevenue(totalRevenue);

            upcomingEventDTOs.add(dto);
        }
        // Sắp xếp danh sách theo revenue giảm dần
        upcomingEventDTOs.sort((a, b) -> Double.compare(b.getRevenue(), a.getRevenue()));

        return upcomingEventDTOs;
    }

    @Override
    public String sentRequestForApproval(int eventId) throws MessagingException {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        if (event.getStatus() != EEventStatus.DRAFT && event.getStatus() != EEventStatus.PENDING
                && event.getStatus() != EEventStatus.REJECTED) {
            throw new AppException(ErrorCode.INVALID_EVENT_STATUS);
        }

        event.setStatus(EEventStatus.PENDING);
        eventRepository.save(event);
        Account manager = accountRepository.findManagerWithLeastVerifications()
                .orElseThrow(() -> new AppException(ErrorCode.MANAGER_NOT_FOUND));
        List<Contract> contractList = contractRepository.findContractsByEvent_EventId(eventId);
        for (Contract contract : contractList) {
            if (contract.getStatus().equals(EContractStatus.APPROVED)) {
                throw new AppException(ErrorCode.EVENT_ALREADY_APPROVED);
            }
        }

        String fullName = event.getOrganizer().getFirstName() + " " + event.getOrganizer().getLastName();
        emailService.sendEventApprovalRequest(manager.getEmail(), event.getEventName(), fullName);

        Notification notification = new Notification();
        notification.setMessage("Có sự kiện mới cần duyệt");
        notification.setAccount(manager);
        notification.setRead(false);
        notification.setCreatedDate(LocalDateTime.now());
        notification.setReadDate(null);

        notificationRepository.saveAndFlush(notification);
        messagingTemplate.convertAndSendToUser(manager.getUserName(), "/specific/messages", "Có sự kiện mới cần duyệt");


        return "Yêu cầu đã được gửi";

    }

    @Override
    public List<EventForConsumerResponse> getEventsForConsumerByStatusScheduled() {
        List<Event> events = eventRepository.findEventsByStatus(EEventStatus.SCHEDULED);
        if (events.isEmpty()) {
            throw new AppException(ErrorCode.EVENT_NOT_FOUND);
        }

        var earliestEventDate = events.stream()
                .flatMap(event -> event.getEventActivities().stream())
                .filter(eventActivity -> eventActivity.getDateEvent().isAfter(LocalDate.now()))
                .map(EventActivity::getDateEvent)  // Chỉ lấy dateEvent
                .min(Comparator.naturalOrder())  // Lấy dateEvent sớm nhất
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_ACTIVITY_NOT_FOUND));  // Nếu không tìm thấy thì throw exception


        List<EventForConsumerResponse> responses = events.stream()
                .map(event -> new EventForConsumerResponse(
                        event.getBannerURL(),
                        event.getEventId(),
                        event.getEventName(),
                        ticketRepository.findMinTicketByEvent_EventId(event.getEventId())
                                .map(Ticket::getPrice)
                                .orElse(0.0),
                        event.getLogoURL(),
                        earliestEventDate

                ))
                .collect(Collectors.toList());

        Collections.reverse(responses); // Đảo ngược danh sách

        return responses;
    }


    @Override
    public EventDetailForConsumer getEventDetailForConsumer(int eventId) {
        Event event = eventRepository.findEventByEventId(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        // Dùng ModelMapper để map sang DTO
        List<EventActivityDTO> eventActivityDTOList = event.getEventActivities().stream()
                .map(activity -> modelMapper.map(activity, EventActivityDTO.class))
                .collect(Collectors.toList());

        // Ánh xạ từ EventActivityDTO sang EventActivityResponse
        List<EventActivityResponse> eventActivityResponseList = modelMapper.map(eventActivityDTOList, new TypeToken<List<EventActivityResponse>>() {
        }.getType());

        // Lấy công ty của sự kiện
        Company company = event.getCompany();

        // Lấy giá vé thấp nhất
        double minPrice = ticketRepository.findMinTicketByEvent_EventId(eventId)
                .map(Ticket::getPrice)
                .orElse(0.0);

        // Duyệt qua từng EventActivityResponse để gán Ticket vào từng sự kiện
        for (EventActivityResponse activityResponse : eventActivityResponseList) {
            // Lấy danh sách TicketMapping liên quan đến EventActivity
            List<TicketMapping> ticketMappingList = ticketMappingRepository.findTicketMappingsByEventActivity_EventActivityId(activityResponse.getEventActivityId());

            // Nếu không có TicketMapping, tiếp tục với việc lấy vé từ các Ticket
            List<TicketDTO> ticketDTOS = new ArrayList<>();

            if (!ticketMappingList.isEmpty()) {
                // Lấy danh sách Ticket từ TicketMapping
                for (TicketMapping ticketMapping : ticketMappingList) {
                    Optional<Ticket> ticketOpt = ticketRepository.findById(ticketMapping.getTicket().getTicketId());
                    ticketOpt.ifPresent(ticket -> ticketDTOS.add(modelMapper.map(ticket, TicketDTO.class)));
                }
            } else {
                // Nếu không có TicketMapping, lấy Ticket trực tiếp từ EventActivity hoặc Zone
                // Bạn có thể thêm logic để lấy vé từ Zone hoặc Seat nếu cần
                // Trong trường hợp này, tôi chỉ lấy Ticket mặc định nếu không có TicketMapping
                List<Ticket> tickets = ticketRepository.findTicketsByEvent_EventId(eventId);
                ticketDTOS.addAll(tickets.stream()
                        .map(ticket -> modelMapper.map(ticket, TicketDTO.class))
                        .collect(Collectors.toList()));
            }
            activityResponse.setSoldOut(!ticketMappingService.checkTicketMappingExist(activityResponse.getEventActivityId(), ticketDTOS.get(0).getTicketId()));

            // Gán danh sách TicketDTO vào EventActivityResponse
            activityResponse.setTickets(ticketDTOS);
        }


        // Kiểm tra xem sự kiện có seat map không
        boolean isHaveSeatMap = seatMapRepository.findSeatMapByEvent_EventId(eventId).isPresent();
        int eventCatetoryId = event.getCategory() != null ? event.getCategory().getEventCategoryId() : 0;
        return new EventDetailForConsumer(
                event.getEventId(),
                event.getEventName(),
                event.getAddress(),
                event.getCity(),
                event.getDistrict(),
                event.getWard(),
                event.getLocationName(),
                event.getLogoURL(),
                event.getBannerURL(),
                company != null ? company.getLogoURL() : null,  // URL logo của công ty
                company != null ? company.getCompanyName() : null, // Tên công ty
                company != null ? company.getDescription() : null, // Mô tả công ty
                event.getStatus().name(),
                event.getTypeEvent(),
                event.getDescription(),
                event.getCategory().getCategoryName(),
                eventCatetoryId,
                eventActivityResponseList,
                isHaveSeatMap,
                minPrice
        );
    }

    @Override
    public boolean countView(int eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
        event.setCountView(event.getCountView() + 1);
        eventRepository.save(event);
        return true;
    }

    @Override
    public List<EventForConsumerResponse> getEventsForConsumerForWeekend() {
        LocalDate today = LocalDate.now();

        // Lấy thứ 7 và chủ nhật của tuần hiện tại (không cần kiểm tra nếu hôm nay là T7/CN nữa)
        LocalDate startWeekend = appUtils.getStartOfWeekend(today);
        LocalDate endWeekend = appUtils.getEndOfWeekend(today);

        List<Event> events = eventRepository.findScheduledEvents().stream()
                .filter(event -> event.getEventActivities().stream()
                        .anyMatch(activity -> {
                            LocalDate date = activity.getDateEvent();
                            return date != null &&
                                    !date.isBefore(startWeekend) &&
                                    !date.isAfter(endWeekend);
                        }))
                .filter(event -> event.getStatus() == EEventStatus.SCHEDULED)
                .toList();

        if (events.isEmpty()) {
            throw new AppException(ErrorCode.EVENT_NOT_FOUND);
        }

        List<EventForConsumerResponse> responses = events.stream()
                .map(event -> new EventForConsumerResponse(
                        event.getBannerURL(),
                        event.getEventId(),
                        event.getEventName(),
                        ticketRepository.findMinTicketByEvent_EventId(event.getEventId())
                                .map(Ticket::getPrice)
                                .orElse(0.0),
                        event.getLogoURL(),
                        event.getEventActivities().stream()
                                .map(EventActivity::getDateEvent)
                                .filter(date -> date != null &&
                                        !date.isBefore(startWeekend) &&
                                        !date.isAfter(endWeekend))
                                .findFirst()
                                .orElse(null)
                ))
                .collect(Collectors.toList());

        Collections.reverse(responses);
        return responses;
    }


    @Override
    public List<EventForConsumerResponse> getEventsForConsumerInMonth(int month) {
        List<Event> events = eventRepository.findEventsByStatus(EEventStatus.SCHEDULED).stream()
                .filter(event -> event.getEventActivities().stream()
                        .anyMatch(eventActivity -> eventActivity.getDateEvent().getMonthValue() == month))
                .toList();

        if (events.isEmpty()) {
            throw new AppException(ErrorCode.EVENT_NOT_FOUND);
        }

        List<EventForConsumerResponse> responses = events.stream()
                .map(event -> new EventForConsumerResponse(
                        event.getBannerURL(),
                        event.getEventId(),
                        event.getEventName(),
                        ticketRepository.findMinTicketByEvent_EventId(event.getEventId())
                                .map(Ticket::getPrice)
                                .orElse(0.0),
                        event.getLogoURL(),
                        event.getEventActivities().stream()
                                .filter(eventActivity -> eventActivity.getDateEvent().getMonthValue() == month)
                                .map(EventActivity::getDateEvent)
                                .findFirst()
                                .orElse(null) // Nếu không tìm thấy ngày nào thì trả về null
                ))
                .collect(Collectors.toList());

        Collections.reverse(responses); // Đảo ngược danh sách

        return responses;
    }


    public PaginationResponse<EventDetailForConsumer> searchEvent(
            String eventName, Integer eventCategoryId, Double minPrice, String city, int page, int size) {
        // Chuẩn hóa input
        String finalEventName = (eventName != null && !eventName.trim().isEmpty()) ? eventName.trim().toLowerCase() : null;
        Integer finalCategoryId = (eventCategoryId != null && eventCategoryId != 0) ? eventCategoryId : null;
        Double finalMinPrice = (minPrice != null && minPrice > 0) ? minPrice : null;
        String finalCity = ("all".equalsIgnoreCase(city)) ? null : (city != null ? city.trim().toLowerCase() : null);

        // Lấy tất cả sự kiện có trạng thái SCHEDULED
        List<Event> events = eventRepository.findEventsByStatus(EEventStatus.SCHEDULED);
        if (events.isEmpty()) {
            log.info("No SCHEDULED events found");
            return new PaginationResponse<>(Collections.emptyList(), page, 0, 0, size);
        }

        // Lọc sự kiện
        List<Event> filteredEvents = events.stream()
                .filter(e -> finalEventName == null || e.getEventName().toLowerCase().contains(finalEventName))
                .filter(e -> finalCategoryId == null ||
                        (e.getCategory() != null && e.getCategory().getEventCategoryId() == finalCategoryId))
                .filter(e -> finalMinPrice == null ||
                        ticketRepository.findMinTicketByEvent_EventId(e.getEventId())
                                .map(t -> t.getPrice() >= finalMinPrice)
                                .orElse(false))
                .filter(e -> {
                    if (finalCity == null) return true;
                    String eventCity = e.getCity() != null ? e.getCity().toLowerCase() : "";
                    if ("other".equalsIgnoreCase(finalCity)) {
                        return !List.of("thành phố hà nội", "thành phố hồ chí minh", "thành phố đà nẵng").contains(eventCity);
                    } else {
                        return eventCity.equals(finalCity);
                    }
                })
                .collect(Collectors.toList());

        if (filteredEvents.isEmpty()) {
            log.info("No events match the filters");
            return new PaginationResponse<>(Collections.emptyList(), page, 0, 0, size);
        }

        // Phân trang trước khi ánh xạ DTO
        int preliminaryTotalElements = filteredEvents.size();
        int preliminaryTotalPages = (int) Math.ceil((double) preliminaryTotalElements / size);
        int validPage = Math.max(0, Math.min(page, preliminaryTotalPages - 1));
        int fromIndex = validPage * size;
        int toIndex = Math.min(fromIndex + size, preliminaryTotalElements);
        List<Event> pagedEvents = fromIndex >= preliminaryTotalElements ? new ArrayList<>() : filteredEvents.subList(fromIndex, toIndex);

        // Chuyển đổi sang DTO
        List<EventDetailForConsumer> dtoList = pagedEvents.stream()
                .map(event -> {
                    try {
                        Company company = event.getCompany();
                        boolean isHaveSeatMap = event.getSeatMap() != null;
                        List<EventActivityDTO> eventActivityDTOList = event.getEventActivities().stream()
                                .map(activity -> modelMapper.map(activity, EventActivityDTO.class))
                                .collect(Collectors.toList());
                        List<EventActivityResponse> eventActivityResponseList = modelMapper.map(
                                eventActivityDTOList, new TypeToken<List<EventActivityResponse>>() {}.getType());
                        double minEventPrice = ticketRepository.findMinTicketByEvent_EventId(event.getEventId())
                                .map(Ticket::getPrice)
                                .orElse(0.0);
                        int eventCategoryId1 = event.getCategory() != null ? event.getCategory().getEventCategoryId() : 0;

                        return new EventDetailForConsumer(
                                event.getEventId(), event.getEventName(), event.getAddress(), event.getWard(),
                                event.getDistrict(), event.getCity(), event.getLocationName(), event.getLogoURL(),
                                event.getBannerURL(), company != null ? company.getLogoURL() : null,
                                company != null ? company.getCompanyName() : null,
                                company != null ? company.getDescription() : null,
                                event.getStatus().name(), event.getTypeEvent(), event.getDescription(),
                                event.getCategory() != null ? event.getCategory().getCategoryName() : null,
                                eventCategoryId1, eventActivityResponseList, isHaveSeatMap, minEventPrice);
                    } catch (Exception e) {
                        log.error("Error mapping event {} to DTO: {}", event.getEventId(), e.getMessage());
                        return null;
                    }
                })
                .filter(dto -> dto != null) // Loại bỏ các DTO null
                .collect(Collectors.toList());

        // Tính lại totalElements và totalPages dựa trên dtoList
        int totalElements = dtoList.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);

        // Đảm bảo validPage không vượt quá totalPages mới
        validPage = Math.max(0, Math.min(page, totalPages - 1));

        // Áp dụng lại phân trang cho dtoList
        fromIndex = validPage * size;
        toIndex = Math.min(fromIndex + size, totalElements);
        List<EventDetailForConsumer> finalDtoList = fromIndex >= totalElements ? new ArrayList<>() : dtoList.subList(fromIndex, toIndex);

        // Ghi log để kiểm tra
        log.info("Filtered events: {}, Paged events: {}, DTO list: {}, Final DTO list: {}, Total elements: {}, Total pages: {}",
                filteredEvents.size(), pagedEvents.size(), dtoList.size(), finalDtoList.size(), totalElements, totalPages);

        return new PaginationResponse<>(finalDtoList, validPage, totalPages, totalElements, size);
    }

    @Override
    public PaginationResponse<EventDashboardResponse> getEventDashboardByCompanyId(int companyId, int page, int size) {
        AppUtils.checkRole(ERole.ORGANIZER);

        // Authenticate user
        var context = SecurityContextHolder.getContext();
        String username = context.getAuthentication().getName();

        var account = accountRepository.findAccountByUserName(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Validate company
        var company = companyRepository.findById(companyId)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_CREATE_COMPANY));

        if (company.getStatus() != ECompanyStatus.ACTIVE) {
            throw new AppException(ErrorCode.COMPANY_NOT_ACTIVE);
        }

        if (company.getCompanyId() != companyId) {
            throw new AppException(ErrorCode.INVALID_COMPANY);
        }

        // Fetch events for the company
        List<Event> events = eventRepository.findEventsByCompany_CompanyId(companyId)
                .orElse(new ArrayList<>());

        // If no events found, return empty paginated response
        if (events.isEmpty()) {
            return new PaginationResponse<>(new ArrayList<>(), page, 0, 0, size);
        }

        List<EventDashboardResponse> eventDashboardResponses = new ArrayList<>();

        // Process each event
        for (Event event : events) {
            EventDashboardResponse response = new EventDashboardResponse();
            response.setEventId(event.getEventId());
            response.setEventName(event.getEventName());
            response.setDescription(event.getDescription());
            response.setAddress(event.getAddress());
            response.setEventCode(event.getEventCode());
            response.setCity(event.getCity());
            response.setDistrict(event.getDistrict());
            response.setWard(event.getWard());
            response.setLocationName(event.getLocationName());
            response.setLogoURL(event.getLogoURL());
            response.setBannerURL(event.getBannerURL());
            response.setStatus(event.getStatus().name());
            response.setCountView(event.getCountView());
            response.setTypeEvent(event.getTypeEvent());
            response.setEventCategory(event.getCategory() != null ? event.getCategory().getCategoryName() : null);
            response.setHaveSeatMap(event.getSeatMap() != null);

            // Map event activities to DTOs
            List<EventActivityDTO> eventActivityDTOList = event.getEventActivities().stream()
                    .map(activity -> modelMapper.map(activity, EventActivityDTO.class))
                    .collect(Collectors.toList());

            // Map to EventActivityResponse
            List<EventActivityResponse> eventActivityResponseList = modelMapper.map(eventActivityDTOList, new TypeToken<List<EventActivityResponse>>() {
            }.getType());

            // Process tickets for each activity
            for (EventActivityResponse activityResponse : eventActivityResponseList) {
                List<TicketMapping> ticketMappingList = ticketMappingRepository.findTicketMappingsByEventActivity_EventActivityId(activityResponse.getEventActivityId());
                List<TicketDTO> ticketDTOS = new ArrayList<>();

                if (!ticketMappingList.isEmpty()) {
                    for (TicketMapping ticketMapping : ticketMappingList) {
                        Optional<Ticket> ticketOpt = ticketRepository.findById(ticketMapping.getTicket().getTicketId());
                        ticketOpt.ifPresent(ticket -> ticketDTOS.add(modelMapper.map(ticket, TicketDTO.class)));
                    }
                } else {
                    List<Ticket> tickets = ticketRepository.findTicketsByEvent_EventId(event.getEventId());
                    ticketDTOS.addAll(tickets.stream()
                            .map(ticket -> modelMapper.map(ticket, TicketDTO.class))
                            .collect(Collectors.toList()));
                }

                activityResponse.setTickets(ticketDTOS);
            }

            response.setEventActivityDTOList(eventActivityResponseList);

            // Calculate total tickets sold and revenue
            Integer totalTicketSold = ticketPurchaseRepository.getTotalTicketsSoldByEventId(event.getEventId());
            Double totalRevenue = ticketPurchaseRepository.getTotalPriceByEventId(event.getEventId());

            response.setCountTicketSold(totalTicketSold);
            response.setTotalRevenue(totalRevenue);

            eventDashboardResponses.add(response);
        }

        // Pagination logic
        int totalElements = eventDashboardResponses.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, totalElements);

        // If fromIndex exceeds totalElements, return empty list with correct metadata
        if (fromIndex > totalElements) {
            return new PaginationResponse<>(new ArrayList<>(), page, totalPages, totalElements, size);
        }

        // If fromIndex equals totalElements, return empty list for that page
        List<EventDashboardResponse> pageItems = fromIndex == totalElements ?
                new ArrayList<>() : eventDashboardResponses.subList(fromIndex, toIndex);

        return new PaginationResponse<>(pageItems, page, totalPages, totalElements, size);
    }    @Override
    public boolean approvedEvent(int eventId, EEventStatus status) throws MessagingException {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        var event = eventRepository.findEventByEventId(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));



        switch (status) {
            case CONFIRMED -> {
                if (event.getStatus() != EEventStatus.PENDING) {
                    throw new AppException(ErrorCode.EVENT_NOT_PENDING_APPROVAL);
                }
                event.setStatus(EEventStatus.CONFIRMED);

                // Gửi email thông báo cho người tổ chức sự kiện
                String fullName = event.getOrganizer().getFirstName() + " " + event.getOrganizer().getLastName();
                emailService.sendEventApprovalNotification(
                        event.getOrganizer().getEmail(),
                        event.getEventName(),
                        fullName
                );

                Notification notification = new Notification();
                notification.setMessage("Sự kiện của bạn đã được phê duyệt");
                notification.setAccount(event.getOrganizer());
                notification.setRead(false);
                notification.setCreatedDate(LocalDateTime.now());
                notification.setReadDate(null);
                notificationRepository.saveAndFlush(notification);
                messagingTemplate.convertAndSendToUser(event.getOrganizer().getUserName(), "/specific/messages", "Sự kiện của bạn đã được phê duyệt");
            }

            case REJECTED -> {
                if (event.getStatus() != EEventStatus.PENDING) {
                    throw new AppException(ErrorCode.EVENT_NOT_PENDING_APPROVAL);
                }
                event.setStatus(EEventStatus.REJECTED);

                // Gửi email thông báo từ chối sự kiện
                String fullName = event.getOrganizer().getFirstName() + " " + event.getOrganizer().getLastName();
                emailService.sendEventRejectionNotification(
                        event.getOrganizer().getEmail(),
                        event.getEventName(),
                        fullName
                );
                Notification notification = new Notification();
                notification.setMessage("Sự kiện của bạn đã bị từ chối");
                notification.setAccount(event.getOrganizer());
                notification.setRead(false);
                notification.setCreatedDate(LocalDateTime.now());
                notification.setReadDate(null);
                notificationRepository.saveAndFlush(notification);
                messagingTemplate.convertAndSendToUser(event.getOrganizer().getUserName(), "/specific/messages", "Sự kiện của bạn đã bị từ chối");
            }
            case CANCELLED -> {
                if(!event.getStatus().equals(EEventStatus.SCHEDULED)){
                    throw new AppException(ErrorCode.EVENT_NOT_SCHEDULED);
                }

                    event.setStatus(EEventStatus.CANCELLED);

                String organizerFullname = event.getOrganizer().getFirstName() + " " + event.getOrganizer().getLastName();
                emailService.sendEventCancellationEmail(
                        event.getOrganizer().getEmail(),
                        organizerFullname,
                        event.getEventName()
                );

                Contract contract = contractRepository.findContractsByEvent_EventId(eventId).get(0);
                if(contract.getContractType().equals(EContractType.INSTALLMENT.name())){
                    throw new AppException(ErrorCode.CANCEL_INSTALLMENT_CONTRACT);
                }
                contract.setStatus(EContractStatus.CANCELLED);
                contractRepository.save(contract);

                Notification notification = new Notification();
                notification.setMessage("Sự kiện của bạn đã bị hủy");
                notification.setAccount(event.getOrganizer());
                notification.setRead(false);
                notification.setCreatedDate(LocalDateTime.now());
                notification.setReadDate(null);
                notificationRepository.saveAndFlush(notification);

                simpMessagingTemplate.convertAndSendToUser(
                        event.getOrganizer().getUserName(),
                        "/specific/messages",
                        "Sự kiện của bạn đã bị hủy"
                );

                List<TicketPurchase> ticketPurchases = ticketPurchaseRepository
                        .findTicketPurchasesByEvent_EventIdAndStatus(eventId, ETicketPurchaseStatus.PURCHASED);

                ExecutorService executor = Executors.newFixedThreadPool(5); // Giới hạn 5 luồng
                Set<String> sentEmails = new HashSet<>(); // Track các email đã gửi

                for (TicketPurchase ticketPurchase : ticketPurchases) {
                    ticketPurchase.setStatus(ETicketPurchaseStatus.REFUNDING);
                    ticketPurchaseRepository.save(ticketPurchase);

                    String email = ticketPurchase.getAccount().getEmail();

                    if (!sentEmails.contains(email)) {
                        sentEmails.add(email); // Đánh dấu đã gửi

                        executor.submit(() -> {
                            try {
                                String buyerFullname = ticketPurchase.getAccount().getFirstName() + " " + ticketPurchase.getAccount().getLastName();
                                emailService.sendEventCancellationEmail(
                                        email,
                                        buyerFullname,
                                        ticketPurchase.getEvent().getEventName()
                                );
                            } catch (Exception e) {
                                log.error("Failed to send cancellation email to " + email, e);
                            }
                        });
                    }
                }

                executor.shutdown(); // Sau khi submit hết thì đóng executor


            }


            default -> throw new AppException(ErrorCode.INVALID_EVENT_STATUS);
        }

        eventRepository.save(event); // Đừng quên lưu lại trạng thái đã cập nhật

        return true;
    }

    @Override
    public List<EventForConsumerResponse> getEventsForConsumerByCountViewTop10() {
        List<Event> events = eventRepository.findEventsByStatus(EEventStatus.SCHEDULED);
        if (events.isEmpty()) {
            throw new AppException(ErrorCode.EVENT_NOT_FOUND);
        }

        // Sắp xếp theo lượt xem giảm dần và lấy top 5
        List<Event> top5Events = events.stream()
                .sorted(Comparator.comparingInt(Event::getCountView).reversed())
                .limit(10)
                .collect(Collectors.toList());

        // Chuyển sang DTO và trả về (không cần reverse nữa nếu đã sort đúng thứ tự)
        return top5Events.stream()
                .map(event -> new EventForConsumerResponse(
                        event.getBannerURL(),
                        event.getEventId(),
                        event.getEventName(),
                        ticketRepository.findMinTicketByEvent_EventId(event.getEventId())
                                .map(Ticket::getPrice)
                                .orElse(0.0),
                        event.getLogoURL(),
                        event.getEventActivities().stream()
                                .filter(eventActivity -> appUtils.isWeekend(eventActivity.getDateEvent()))
                                .map(EventActivity::getDateEvent)
                                .findFirst()
                                .orElse(null) // Nếu không tìm thấy ngày nào thì trả về null
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<CompanyDashboardResponse> eventDashboard(int eventId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Event event = eventRepository
                .findEventByEventIdAndCompany_RepresentativeId_UserName(eventId, username)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        if (!(event.getStatus().equals(EEventStatus.SCHEDULED)
                || event.getStatus().equals(EEventStatus.COMPLETED)
                || event.getStatus().equals(EEventStatus.CONFIRMED))) {
            throw new AppException(ErrorCode.EVENT_NOT_APPROVED);
        }

        List<EventActivityDashbroadResponse> activityDashboardList = new ArrayList<>();
        List<CompanyDashboardResponse.EventActivityDateDashbroadResponse> activityRevenueDateList = new ArrayList<>();
        Map<String, Double> ticketRevenueMap = new HashMap<>();

        for (EventActivity activity : event.getEventActivities()) {
            // Lấy khoảng thời gian bán vé
            LocalDate earliestStart = activity.getStartTicketSale().toLocalDate();
            LocalDate latestEnd = activity.getEndTicketSale().toLocalDate();

            // Lấy doanh thu theo ngày
            List<RevenueByDateProjection> dailyRevenue = orderRepository
                    .getRevenueByEventIdAndEventActivityIdAndDateRange(
                            event.getEventId(),
                            activity.getEventActivityId(),
                            earliestStart,
                            latestEnd
                    );

//            List<RevenueByDateProjection> dailyRevenue = ticketPurchaseRepository
//                    .getDailyRevenueByEventAndActivity(
//                            event.getEventId(),
//                            activity.getEventActivityId(),
//
//                    );

            // Mapping doanh thu theo ngày
            List<CompanyDashboardResponse.EventActivityRevenueReportResponse> activityRevenueList =
                    dailyRevenue.stream()
                            .map(r -> new CompanyDashboardResponse.EventActivityRevenueReportResponse(
                                    r.getOrderDay().toString(), r.getTotalRevenue()))
                            .collect(Collectors.toList());

            activityRevenueDateList.add(new CompanyDashboardResponse.EventActivityDateDashbroadResponse(
                    activity.getActivityName(), activityRevenueList
            ));

            // Lấy danh sách vé của activity
            List<Ticket> tickets;
            List<TicketMapping> mappings = ticketMappingRepository
                    .findTicketMappingsByEventActivity_EventActivityId(activity.getEventActivityId());

            if (!mappings.isEmpty()) {
                tickets = mappings.stream()
                        .map(TicketMapping::getTicket)
                        .collect(Collectors.toList());
            } else {
                tickets = ticketRepository.findTicketsByEvent_EventId(event.getEventId());
            }

            List<EventActivityDashbroadResponse.TicketDashBoardResponse> ticketDashboard = new ArrayList<>();

            for (Ticket ticket : tickets) {
                double ticketPrice = ticket.getPrice();
                int soldCount = ticketPurchaseRepository
                        .countTicketPurchasedByEventActivityIdAndTicketId(
                                activity.getEventActivityId(), ticket.getTicketId());

                double totalRevenue = ticketPrice * soldCount;
                ticketRevenueMap.merge(ticket.getTicketName(), totalRevenue, Double::sum);

                ticketDashboard.add(new EventActivityDashbroadResponse.TicketDashBoardResponse(
                        ticket.getTicketName(), soldCount));
            }

            activityDashboardList.add(new EventActivityDashbroadResponse(
                    activity.getActivityName(), ticketDashboard
            ));
        }

        List<CompanyDashboardResponse.TicketReVenueDashBoardResponse> ticketRevenueList = ticketRevenueMap.entrySet()
                .stream()
                .map(entry -> new CompanyDashboardResponse.TicketReVenueDashBoardResponse(
                        entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        CompanyDashboardResponse finalResponse = new CompanyDashboardResponse();
        finalResponse.setEventActivityDashbroadResponseList(activityDashboardList);
        finalResponse.setTicketReVenueDashBoardResponseList(ticketRevenueList);
        finalResponse.setEventActivityRevenueReportResponseList(activityRevenueDateList);

        return List.of(finalResponse);
    }


    @Override
    public CheckinStatsResponse getCheckinByEventActivityId(int eventActivityId) {
//        AppUtils.checkRole(ERole.ORGANIZER);
        var eventActivity = eventActivityRepository.findById(eventActivityId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_ACTIVITY_NOT_FOUND));

//        int countCheckinAll = checkinLogRepository.countByTicketPurchase_EventActivity_EventActivityId(eventActivityId);
//        int countCheckin = checkinLogRepository.countByTicketPurchase_EventActivity_EventActivityIdAndCheckinStatus(eventActivityId,ECheckinLogStatus.CHECKED_IN);
//        int countCheckinNot = checkinLogRepository.countByTicketPurchase_EventActivity_EventActivityIdAndCheckinStatus(eventActivityId,ECheckinLogStatus.PENDING);
        int countCheckin = checkinLogRepository.countTotalCheckinsByEventActivityIdAndCheckinStatus(eventActivityId, String.valueOf(ECheckinLogStatus.CHECKED_IN));
        int countCheckinAll= checkinLogRepository.countTotalCheckinsByEventActivityId(eventActivityId);
        int countCheckinNot = checkinLogRepository.countTotalCheckinsByEventActivityIdAndCheckinStatus(eventActivityId, String.valueOf(ECheckinLogStatus.PENDING));
        return new CheckinStatsResponse(
                countCheckinAll,
                countCheckin,
                countCheckinNot
        );
    }

    @Override
    public CheckinByTicketTypeResponse getCheckinByTicketType(int eventActivityId) {
        AppUtils.checkRole(ERole.ORGANIZER);
        var eventActivity = eventActivityRepository.findById(eventActivityId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_ACTIVITY_NOT_FOUND));

        List<TicketCheckinStatsProjection> stats = checkinLogRepository.getTicketCheckinStatsByEventActivityId(eventActivityId);

        if (stats.isEmpty()) {
            throw new AppException(ErrorCode.TICKET_MAPPING_NOT_FOUND);
        }

        List<CheckinByTicketTypeResponse.TicketTypeCheckinStat> checkinStats = stats.stream()
                .map(s -> {
                    int total = s.getTotalPurchased() != null ? s.getTotalPurchased() : 0;
                    int checkedIn = s.getCheckedIn() != null ? s.getCheckedIn() : 0;

                    Ticket ticket = ticketRepository.findById(s.getTicketId())
                            .orElseThrow(() -> new AppException(ErrorCode.TICKET_NOT_FOUND));
                    Integer totalTicket = ticketPurchaseRepository.countTicketPurchaseByEventActivity_EventActivityIdAndStatus(eventActivityId,ETicketPurchaseStatus.PURCHASED);
                    if(totalTicket == null){
                        totalTicket = 0;
                    }
                    double percentage = total > 0 ? (checkedIn * 100.0) / totalTicket : 0.0;
                    return new CheckinByTicketTypeResponse.TicketTypeCheckinStat(
                            s.getTicketName(),
                            ticket.getPrice(),
                            checkedIn,
                            total,
                            totalTicket,
                            Math.round(percentage * 100.0) / 100.0 // làm tròn 2 chữ số
                    );
                })
                .toList();

        return new CheckinByTicketTypeResponse(eventActivityId, checkinStats);
    }

    @Override
    public List<EventForConsumerResponse> getEventsForConsumerByEventCategory(int eventCategoryId, EEventStatus status) {
        List<Event> events = eventRepository.findEventsByCategory_EventCategoryIdAndStatus(eventCategoryId, status);
        if (events.isEmpty()) {
            throw new AppException(ErrorCode.EVENT_NOT_FOUND);
        }


        // Chuyển sang DTO và trả về
        return events.stream()
                .map(event -> new EventForConsumerResponse(
                        event.getBannerURL(),
                        event.getEventId(),
                        event.getEventName(),
                        ticketRepository.findMinTicketByEvent_EventId(event.getEventId())
                                .map(Ticket::getPrice)
                                .orElse(0.0),
                        event.getLogoURL(),
                        event.getEventActivities().stream()
                                .map(EventActivity::getDateEvent)
                                .filter(date -> !date.isBefore(LocalDate.now())) // chỉ lấy từ hôm nay trở đi
                                .min(Comparator.naturalOrder()) // lấy ngày gần nhất
                                .orElse(null)
                ))
                .collect(Collectors.toList());
    }

    @Override
    public DashboardEventResponse getDashboardEvent(int eventId) {
        AppUtils.checkRole(ERole.ORGANIZER);
        var event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        DashboardEventResponse response = new DashboardEventResponse();
        response.setCountViewer(event.getCountView());
        response.setCountTicket(ticketPurchaseRepository.getTotalTicketsSoldByEventId(eventId));
        response.setTotalRevenue(ticketPurchaseRepository.getTotalPriceByEventId(eventId));
        response.setCountOrder(ticketPurchaseRepository.countByStatusAndEvent_EventId(ETicketPurchaseStatus.PURCHASED, eventId));

        return response;
    }

    @Override
    public List<ListCosumerResponse> getCustomerByEventId(int eventActivityId) {
        // Kiểm tra quyền của user (Role)
        AppUtils.checkRole(ERole.ORGANIZER);

        // Kiểm tra sự tồn tại của eventActivity
        var event = eventActivityRepository.findById(eventActivityId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        // Lấy danh sách accountId đã mua vé với status là 'PURCHASED'
        List<Integer> accountIds = ticketPurchaseRepository
                .findDistinctAccountIdsByEventActivityIdAndStatus(eventActivityId, ETicketPurchaseStatus.PURCHASED);

        List<ListCosumerResponse> result = new ArrayList<>();

        // Duyệt qua các accountId để lấy thông tin từng người
        for (Integer accountId : accountIds) {
            var account = accountRepository.findById(accountId)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

            // Lấy danh sách vé đã mua cho từng account theo eventActivityId và status là 'PURCHASED'
            List<TicketPurchase> ticketPurchases = ticketPurchaseRepository
                    .findTicketPurchasesByAccount_AccountIdAndEventActivity_EventActivityIdAndStatus(
                            accountId, eventActivityId, ETicketPurchaseStatus.PURCHASED);

            // Build danh sách TicketPurchaseResponse từ ticketPurchases
            List<TicketSheetResponse> ticketSheets = new ArrayList<>();
            Map<String, List<TicketSheetResponse.TicketPurchaseResponse>> orderCodeMap = new HashMap<>();

            // Group theo orderCode và convert từng vé thành TicketPurchaseResponse
            for (TicketPurchase tp : ticketPurchases) {
                String orderCode = tp.getOrderCode();
                TicketSheetResponse.TicketPurchaseResponse purchaseResponse = new TicketSheetResponse.TicketPurchaseResponse(
                        tp.getTicketPurchaseId(),
                        tp.getTicket().getPrice(),
                        tp.getSeatActivity() != null ? tp.getSeatActivity().getSeat().getSeatName() : null,
                        tp.getTicket().getTicketName(),
                        tp.getZoneActivity().getZone().getZoneName(),
                        tp.getQuantity()
                );

                // Group vé theo orderCode
                orderCodeMap.computeIfAbsent(orderCode, k -> new ArrayList<>()).add(purchaseResponse);
            }

            // Convert orderCodeMap thành TicketSheetResponse
            for (Map.Entry<String, List<TicketSheetResponse.TicketPurchaseResponse>> entry : orderCodeMap.entrySet()) {
                String orderCode = entry.getKey();
                boolean isHaveCheckin = false;

                Optional<CheckinLog> checkin = checkinLogRepository.findCheckinLogByOrder_OrderCode(orderCode);
                if (checkin.isPresent() && checkin.get().getCheckinStatus() == ECheckinLogStatus.CHECKED_IN) {
                    isHaveCheckin = true;
                }

                TicketSheetResponse ticketSheetResponse = new TicketSheetResponse(
                        orderCode,
                        isHaveCheckin,
                        entry.getValue()
                );
                ticketSheets.add(ticketSheetResponse);
            }


            // Build ListCosumerResponse cho mỗi khách hàng
            ListCosumerResponse consumerResponse = ListCosumerResponse.builder()
                    .username(account.getFirstName() + " " + account.getLastName())
                    .email(account.getEmail())
                    .phone(account.getPhone())
                    .CCCD(null) // Nếu có thông tin CCCD
                    .MSSV(null) // Nếu có thông tin MSSV
                    .ticketPurchases(ticketSheets)
                    .build();

            result.add(consumerResponse);
        }

        return result;
    }

}






