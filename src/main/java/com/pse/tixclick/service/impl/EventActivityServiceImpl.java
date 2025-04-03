package com.pse.tixclick.service.impl;

import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.exception.ErrorCode;
import com.pse.tixclick.payload.dto.EventActivityDTO;
import com.pse.tixclick.payload.entity.entity_enum.ESubRole;
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
    public List<CreateEventActivityAndTicketRequest> createEventActivityAndTicket(List<CreateEventActivityAndTicketRequest> requestList) {
        if (requestList == null || requestList.isEmpty()) {
            throw new IllegalArgumentException("Request list cannot be empty");
        }

        var context = SecurityContextHolder.getContext();
        String userName = context.getAuthentication().getName();

        var account = accountRepository.findAccountByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));

        List<CreateEventActivityAndTicketRequest> savedRequests = new ArrayList<>();

        for (CreateEventActivityAndTicketRequest request : requestList) {
            var eventActivity = eventActivityRepository.findEventActivitiesByEvent_EventIdAndActivityName(request.getEventId(), request.getActivityName());
            if (eventActivity.isPresent()) {
                throw new AppException(ErrorCode.ACTIVITY_EXISTED);
            }

            // Kiểm tra sự tồn tại của event
            var event = eventRepository.findById(request.getEventId())
                    .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

            // Tạo và lưu EventActivity
            EventActivity newEventActivity = new EventActivity();
            newEventActivity.setActivityName(request.getActivityName());
            newEventActivity.setDateEvent(request.getDateEvent());
            newEventActivity.setStartTimeEvent(request.getStartTimeEvent());
            newEventActivity.setEndTimeEvent(request.getEndTimeEvent());
            newEventActivity.setStartTicketSale(request.getStartTicketSale());
            newEventActivity.setEndTicketSale(request.getEndTicketSale());
            newEventActivity.setEvent(event);
            newEventActivity.setCreatedBy(account);
            eventActivityRepository.save(newEventActivity);

            // Kiểm tra nếu request có tickets thì mới xử lý Ticket và TicketMapping
            if (request.getTickets() != null && !request.getTickets().isEmpty()) {
                List<Ticket> tickets = new ArrayList<>();
                for (CreateEventActivityAndTicketRequest.TicketRequest ticketRequest : request.getTickets()) {
                    // Kiểm tra xem Ticket có tồn tại không, nếu không thì tạo mới
                    Ticket ticket = ticketRepository.findTicketByTicketCode(ticketRequest.getTicketCode())
                            .orElseGet(() -> {
                                Ticket newTicket = new Ticket();
                                newTicket.setTicketName(ticketRequest.getTicketName());
                                newTicket.setTicketCode(ticketRequest.getTicketCode());
                                newTicket.setCreatedDate(ticketRequest.getCreatedDate() != null ? ticketRequest.getCreatedDate() : LocalDateTime.now());
                                newTicket.setPrice(ticketRequest.getPrice());
                                newTicket.setMinQuantity(ticketRequest.getMinQuantity());
                                newTicket.setMaxQuantity(ticketRequest.getMaxQuantity());
                                newTicket.setStatus(ticketRequest.isStatus());
                                newTicket.setEvent(event);
                                newTicket.setAccount(account);
                                return ticketRepository.save(newTicket);
                            });

                    // Tạo TicketMapping
                    TicketMapping ticketMapping = new TicketMapping();
                    ticketMapping.setEventActivity(newEventActivity);
                    ticketMapping.setTicket(ticket);
                    ticketMapping.setQuantity(ticketRequest.getQuantity());
                    ticketMapping.setStatus(true);
                    ticketMappingRepository.save(ticketMapping);

                    tickets.add(ticket);
                }
            }

            savedRequests.add(request);
        }

        return savedRequests; // Trả về toàn bộ danh sách đã xử lý
    }


}
