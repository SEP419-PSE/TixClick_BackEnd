package com.pse.tixclick.service.impl;

import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.exception.ErrorCode;
import com.pse.tixclick.payload.dto.EventActivityDTO;
import com.pse.tixclick.payload.entity.entity_enum.EEventStatus;
import com.pse.tixclick.payload.entity.entity_enum.ERole;
import com.pse.tixclick.payload.entity.entity_enum.ESubRole;
import com.pse.tixclick.payload.entity.event.Event;
import com.pse.tixclick.payload.entity.event.EventActivity;
import com.pse.tixclick.payload.entity.ticket.Ticket;
import com.pse.tixclick.payload.entity.ticket.TicketMapping;
import com.pse.tixclick.payload.request.CreateEventActivityAndTicketRequest;
import com.pse.tixclick.payload.request.create.CreateEventActivityRequest;
import com.pse.tixclick.repository.*;
import com.pse.tixclick.service.EventActivityService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class EventActivityServiceImpl implements EventActivityService {
    AccountRepository accountRepository;
    EventActivityRepository eventActivityRepository;
    EventRepository eventRepository;
    MemberRepository memberRepository;
    ModelMapper modelMapper;
    SeatMapRepository   seatMapRepository;
    TicketRepository ticketRepository;
    TicketMappingRepository ticketMappingRepository;
    ContractRepository contractRepository;
    @Override
    public EventActivityDTO createEventActivity(CreateEventActivityRequest eventActivityRequest) {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        var organizer = accountRepository.findAccountByUserName(name)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        var seatmap = seatMapRepository.findById(eventActivityRequest.getSeatmapId())
                .orElseThrow(() -> new AppException(ErrorCode.SEATMAP_NOT_FOUND));

        var event = eventRepository.findById(eventActivityRequest.getEventId())
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        var member = memberRepository.findMemberByAccount_UserNameAndCompany_CompanyId(name,event.getCompany().getCompanyId())
                .orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND));
        if (member.getSubRole() != ESubRole.OWNER && member.getSubRole() != ESubRole.ADMIN) {
            throw new AppException(ErrorCode.NOT_PERMISSION);
        }

        boolean isHaveActivity = eventActivityRepository
                .findEventActivityByActivityNameAndEvent_EventId
                        (eventActivityRequest.getActivityName(), eventActivityRequest.getEventId());
        if (isHaveActivity)
        {
            throw new AppException(ErrorCode.ACTIVITY_EXISTED);
        }

        var eventActivity = new EventActivity();
        eventActivity.setActivityName(eventActivityRequest.getActivityName());;
        eventActivity.setCreatedBy(organizer);
        eventActivity.setEvent(event);
        eventActivity.setDateEvent(eventActivityRequest.getDate());
        eventActivity.setStartTimeEvent(eventActivityRequest.getStartTime());
        eventActivity.setEndTimeEvent(eventActivityRequest.getEndTime());
        eventActivity.setSeatMap(seatmap);
        eventActivityRepository.save(eventActivity);
        return modelMapper.map(eventActivity, EventActivityDTO.class);
    }

    @Override
    public EventActivityDTO updateEventActivity(CreateEventActivityRequest eventActivityRequest, int id) {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        var organizer = accountRepository.findAccountByUserName(name)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        var eventActivity = eventActivityRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ACTIVITY_NOT_FOUND));

        var seatmap = seatMapRepository.findById(eventActivityRequest.getSeatmapId())
                .orElseThrow(() -> new AppException(ErrorCode.SEATMAP_NOT_FOUND));
        // Chỉ cập nhật nếu giá trị không rỗng (null)
        if (eventActivityRequest.getActivityName() != null && !eventActivityRequest.getActivityName().isEmpty()) {
            eventActivity.setActivityName(eventActivityRequest.getActivityName());
        }


            var event = eventRepository.findById(eventActivityRequest.getEventId())
                    .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
            eventActivity.setEvent(event);


        if (eventActivityRequest.getDate() != null) {
            eventActivity.setDateEvent(eventActivityRequest.getDate());
        }

        if (eventActivityRequest.getStartTime() != null) {
            eventActivity.setStartTimeEvent(eventActivityRequest.getStartTime());
        }

        if (eventActivityRequest.getEndTime() != null) {
            eventActivity.setEndTimeEvent(eventActivityRequest.getEndTime());
        }
        eventActivity.setSeatMap(seatmap);

        eventActivity.setCreatedBy(organizer);

        eventActivityRepository.save(eventActivity);

        return modelMapper.map(eventActivity, EventActivityDTO.class);
    }

    @Override
    public boolean deleteEventActivity(int id) {
        var eventActivity = eventActivityRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ACTIVITY_NOT_FOUND));
        eventActivityRepository.delete(eventActivity);
        return true;
    }

    @Override
    public List<EventActivityDTO> getEventActivityByEventId(int eventId) {
        List<EventActivity> eventActivities = eventActivityRepository.findEventActivitiesByEvent_EventId(eventId);
        if(eventActivities.isEmpty()) {
            throw new AppException(ErrorCode.ACTIVITY_NOT_FOUND);
        }

        return modelMapper.map(eventActivities, new TypeToken<List<EventActivityDTO>>() {}.getType());
    }
    @Override
    public List<CreateEventActivityAndTicketRequest> createEventActivityAndTicket(List<CreateEventActivityAndTicketRequest> requestList, String contractCode) {
        if (requestList == null || requestList.isEmpty()) {
            throw new IllegalArgumentException("Request list cannot be empty");
        }

        var context = SecurityContextHolder.getContext();
        String userName = context.getAuthentication().getName();

        var account = accountRepository.findAccountByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));

        List<CreateEventActivityAndTicketRequest> savedRequests = new ArrayList<>();

        for (CreateEventActivityAndTicketRequest request : requestList) {
            Event event = eventRepository.findById(request.getEventId())
                    .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

            if (event.getStatus() == EEventStatus.DRAFT) {
                var member = memberRepository.findMemberByAccount_UserNameAndCompany_CompanyId(
                        userName,
                        event.getCompany().getCompanyId()
                ).orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND));

                if (member.getSubRole() != ESubRole.OWNER && member.getSubRole() != ESubRole.ADMIN) {
                    throw new AppException(ErrorCode.NOT_PERMISSION);
                }

            } else if (event.getStatus() == EEventStatus.SCHEDULED) {
                if (contractCode == null) {
                    throw new AppException(ErrorCode.CONTRACT_NOT_FOUND);
                }
                var contract = contractRepository.findByContractCode(contractCode);
                if (contract == null) {
                    throw new AppException(ErrorCode.CONTRACT_NOT_FOUND);
                }

                if (contract.getAccount().getAccountId() != account.getAccountId()) {
                    throw new AppException(ErrorCode.NOT_PERMISSION);
                }

                if (!ERole.MANAGER.equals(account.getRole().getRoleName())) {
                    throw new AppException(ErrorCode.NOT_PERMISSION);
                }
            } else {
                throw new AppException(ErrorCode.CAN_NOT_UPDATE);
            }

            EventActivity eventActivity;

            if (request.getEventActivityId() != null) {
                // ✅ Cập nhật EventActivity
                eventActivity = eventActivityRepository.findById(request.getEventActivityId())
                        .orElseThrow(() -> new AppException(ErrorCode.EVENT_ACTIVITY_NOT_FOUND));

                eventActivity.setActivityName(request.getActivityName());
                eventActivity.setDateEvent(request.getDateEvent());
                eventActivity.setStartTimeEvent(request.getStartTimeEvent());
                eventActivity.setEndTimeEvent(request.getEndTimeEvent());
                eventActivity.setStartTicketSale(request.getStartTicketSale());
                eventActivity.setEndTicketSale(request.getEndTicketSale());
                if (event.getStatus() == EEventStatus.SCHEDULED) {
                    eventActivity.setUpdatedByManager(account);
                }
                eventActivityRepository.saveAndFlush(eventActivity);

            } else {
                // ✅ Xử lý xóa như cũ
                List<EventActivity> eventActivities = eventActivityRepository.findEventActivitiesByEvent_EventId(request.getEventId());

                // ✅ Tạo mới EventActivity
                eventActivity = new EventActivity();
                eventActivity.setActivityName(request.getActivityName());
                eventActivity.setDateEvent(request.getDateEvent());
                eventActivity.setStartTimeEvent(request.getStartTimeEvent());
                eventActivity.setEndTimeEvent(request.getEndTimeEvent());
                eventActivity.setStartTicketSale(request.getStartTicketSale());
                eventActivity.setEndTicketSale(request.getEndTicketSale());
                eventActivity.setCreatedBy(event.getCompany().getRepresentativeId());
                eventActivity.setEvent(event);
                if (event.getStatus().equals(EEventStatus.SCHEDULED)) {
                    eventActivity.setUpdatedByManager(account);
                }
                eventActivityRepository.saveAndFlush(eventActivity);

                request.setEventActivityId(eventActivity.getEventActivityId());

                // ✅ Tạo Ticket và TicketMapping khi tạo mới
                if (request.getTickets() != null && !request.getTickets().isEmpty()) {
                    for (CreateEventActivityAndTicketRequest.TicketRequest ticketRequest : request.getTickets()) {
                        Ticket ticket = ticketRepository.findTicketByTicketCode(ticketRequest.getTicketCode())
                                .orElseGet(() -> {
                                    Ticket newTicket = new Ticket();
                                    newTicket.setTicketName(ticketRequest.getTicketName());
                                    newTicket.setTicketCode(ticketRequest.getTicketCode());
                                    newTicket.setPrice(ticketRequest.getPrice());
                                    newTicket.setMinQuantity(ticketRequest.getMinQuantity());
                                    newTicket.setMaxQuantity(ticketRequest.getMaxQuantity());
                                    newTicket.setEvent(event);
                                    newTicket.setAccount(account);
                                    return ticketRepository.save(newTicket);
                                });

                        TicketMapping ticketMapping = new TicketMapping();
                        ticketMapping.setEventActivity(eventActivity);
                        ticketMapping.setTicket(ticket);
                        ticketMapping.setQuantity(ticketRequest.getQuantity());
                        ticketMapping.setStatus(true);
                        ticketMappingRepository.save(ticketMapping);
                    }
                }
            }

            savedRequests.add(request);
        }

        return savedRequests;
    }

    @Override
    public List<CreateEventActivityAndTicketRequest> getEventActivityAndTicketByEventId(int eventId) {
        var event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        List<EventActivity> eventActivities = eventActivityRepository.findEventActivitiesByEvent_EventId(eventId);
        if (eventActivities.isEmpty()) {
            throw new AppException(ErrorCode.ACTIVITY_NOT_FOUND);
        }

        List<CreateEventActivityAndTicketRequest> eventActivityAndTicketRequests = new ArrayList<>();

        for (EventActivity eventActivity : eventActivities) {
            CreateEventActivityAndTicketRequest request = new CreateEventActivityAndTicketRequest();
            request.setEventId(event.getEventId());
            request.setEventActivityId(eventActivity.getEventActivityId());
            request.setActivityName(eventActivity.getActivityName());
            request.setDateEvent(eventActivity.getDateEvent());
            request.setStartTimeEvent(eventActivity.getStartTimeEvent());
            request.setEndTimeEvent(eventActivity.getEndTimeEvent());
            request.setStartTicketSale(eventActivity.getStartTicketSale());
            request.setEndTicketSale(eventActivity.getEndTicketSale());

            // Kiểm tra seatmap
            boolean hasSeatMap = seatMapRepository.findSeatMapByEvent_EventId(event.getEventId()).isPresent();

            // Nếu không có seatmap thì lấy ticket
            if (!hasSeatMap) {
                // Lấy các ticket mapping theo event activity
                List<TicketMapping> ticketMappings = ticketMappingRepository.findTicketMappingsByEventActivity(eventActivity);

                // Duyệt từng ticket mapping để tạo ticket request
                List<CreateEventActivityAndTicketRequest.TicketRequest> ticketRequests = ticketMappings.stream()
                        .map(mapping -> {
                            Ticket ticket = mapping.getTicket();
                            return CreateEventActivityAndTicketRequest.TicketRequest.builder()
                                    .ticketName(ticket.getTicketName())
                                    .ticketCode(ticket.getTicketCode())
                                    .quantity(mapping.getQuantity()) // dùng quantity từ TicketMapping
                                    .price(ticket.getPrice())
                                    .minQuantity(ticket.getMinQuantity())
                                    .maxQuantity(ticket.getMaxQuantity())
                                    .eventId(eventId)
                                    .build();
                        })
                        .toList();

                request.setTickets(ticketRequests);
            }
            eventActivityAndTicketRequests.add(request);

        }
        return eventActivityAndTicketRequests;
    }

}
