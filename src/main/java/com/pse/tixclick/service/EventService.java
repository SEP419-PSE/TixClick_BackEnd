package com.pse.tixclick.service;

import com.pse.tixclick.payload.dto.EventDTO;
import com.pse.tixclick.payload.dto.UpcomingEventDTO;
import com.pse.tixclick.payload.entity.entity_enum.EEventStatus;
import com.pse.tixclick.payload.request.create.CreateEventRequest;
import com.pse.tixclick.payload.request.update.UpdateEventRequest;
import com.pse.tixclick.payload.response.*;
import jakarta.mail.MessagingException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface EventService {
    EventDTO createEvent(CreateEventRequest eventDTO, MultipartFile logoURL, MultipartFile bannerURL) throws IOException;

    EventDTO updateEvent(UpdateEventRequest eventRequest, MultipartFile logoURL, MultipartFile bannerURL) throws IOException;

    boolean deleteEvent(int id);

    List<EventResponse> getAllEvent();

    List<EventResponse> getAllEventScheduledAndPendingApproved();

    EventDTO getEventById(int id);

    List<EventDTO> getEventByStatus(EEventStatus status);

    List<EventDTO> getEventByDraft();

    List<EventDTO> getEventByCompleted();


    List<EventDTO> getAllEventsByAccountId();

    List<EventDTO> getEventsByAccountIdAndStatus(String status);

    List<EventDTO> getEventsByCompanyId(int companyId);

    int countTotalScheduledEvents();

    double getAverageTicketPrice();

    Map<String, Double> getEventCategoryDistribution();

    List<UpcomingEventDTO> getUpcomingEvents();

    List<UpcomingEventDTO> getTopPerformingEvents();

    String sentRequestForApproval(int eventId) throws MessagingException;

    List<EventForConsumerResponse> getEventsForConsumerByStatusScheduled();

    EventDetailForConsumer getEventDetailForConsumer(int eventId);

    boolean countView(int eventId);

    List<EventForConsumerResponse> getEventsForConsumerForWeekend();

    List<EventForConsumerResponse> getEventsForConsumerInMonth(int month);

    List<EventDetailForConsumer>  getEventByStartDateAndEndDateAndEventTypeAndEventName(String startDate, String endDate, String eventType, String eventName,List<String> eventCategory, Double minPrice, Double maxPrice);

    List<EventDetailForConsumer> searchEvent(String eventName, Integer eventCategoryId, Double minPrice, String city);
    PaginationResponse<EventDashboardResponse> getEventDashboardByCompanyId(int companyId, int page, int size);

    boolean approvedEvent(int eventId, EEventStatus status) throws MessagingException;

    List<EventForConsumerResponse> getEventsForConsumerByCountViewTop10();

    List<CompanyDashboardResponse> eventDashboard(int eventId);

    CheckinStatsResponse getCheckinByEventActivityId(int eventActivityId);

    CheckinByTicketTypeResponse getCheckinByTicketType(int eventActivityId);

    List<EventForConsumerResponse> getEventsForConsumerByEventCategory(int eventCategoryId,EEventStatus status);

    DashboardEventResponse getDashboardEvent(int eventId);
}
