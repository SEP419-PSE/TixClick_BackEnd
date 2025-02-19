package com.pse.tixclick.service.impl;

import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.exception.ErrorCode;
import com.pse.tixclick.payload.dto.EventActivityDTO;
import com.pse.tixclick.payload.entity.event.EventActivity;
import com.pse.tixclick.payload.request.create.CreateEventActivityRequest;
import com.pse.tixclick.repository.AccountRepository;
import com.pse.tixclick.repository.EventActivityRepository;
import com.pse.tixclick.repository.EventRepository;
import com.pse.tixclick.service.EventActivityService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EventActivityServiceImpl implements EventActivityService {
    AccountRepository accountRepository;
    EventActivityRepository eventActivityRepository;
    EventRepository eventRepository;
    ModelMapper modelMapper;
    @Override
    public EventActivityDTO createEventActivity(CreateEventActivityRequest eventActivityRequest) {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();
        var organizer = accountRepository.findAccountByUserName(name)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        var event = eventRepository.findById(eventActivityRequest.getEventId())
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
        var eventActivity = new EventActivity();
        eventActivity.setActivityName(eventActivityRequest.getActivityName());;
        eventActivity.setCreatedBy(organizer);
        eventActivity.setEvent(event);
        eventActivity.setDate(eventActivityRequest.getDate());
        eventActivity.setStartTime(eventActivityRequest.getStartTime());
        eventActivity.setEndTime(eventActivityRequest.getEndTime());
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

        // Chỉ cập nhật nếu giá trị không rỗng (null)
        if (eventActivityRequest.getActivityName() != null && !eventActivityRequest.getActivityName().isEmpty()) {
            eventActivity.setActivityName(eventActivityRequest.getActivityName());
        }


            var event = eventRepository.findById(eventActivityRequest.getEventId())
                    .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
            eventActivity.setEvent(event);


        if (eventActivityRequest.getDate() != null) {
            eventActivity.setDate(eventActivityRequest.getDate());
        }

        if (eventActivityRequest.getStartTime() != null) {
            eventActivity.setStartTime(eventActivityRequest.getStartTime());
        }

        if (eventActivityRequest.getEndTime() != null) {
            eventActivity.setEndTime(eventActivityRequest.getEndTime());
        }

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
        List<EventActivity> eventActivities = eventActivityRepository.findEventActivitiesByEvent_EventId(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.ACTIVITY_NOT_FOUND));

        return modelMapper.map(eventActivities, new TypeToken<List<EventActivityDTO>>() {}.getType());
    }

}
