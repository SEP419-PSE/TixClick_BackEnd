package com.pse.tixclick.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.pse.tixclick.config.Util;
import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.exception.ErrorCode;
import com.pse.tixclick.payload.dto.EventDTO;
import com.pse.tixclick.payload.entity.event.Event;
import com.pse.tixclick.payload.request.create.CreateEventRequest;
import com.pse.tixclick.payload.request.update.UpdateEventRequest;
import com.pse.tixclick.repository.AccountRepository;
import com.pse.tixclick.repository.EventCategoryRepository;
import com.pse.tixclick.repository.EventRepository;
import com.pse.tixclick.service.EventService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EventServiceImpl implements EventService {
    EventRepository eventRepository;
    ModelMapper modelMapper;
    EventCategoryRepository eventCategoryRepository;
    AccountRepository accountRepository;
    Cloudinary cloudinary;

    @Autowired
    Util util;

    @Override
    public EventDTO createEvent(CreateEventRequest request, MultipartFile logoURL, MultipartFile bannerURL) throws IOException {
        if (request == null || request.getEventName() == null || request.getCategoryId() == 0) {
            throw new AppException(ErrorCode.INVALID_EVENT_DATA);
        }
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();
        var organnizer = accountRepository.findAccountByUserName(name)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        var category = eventCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        // Upload từng ảnh lên Cloudinary
        String logocode = uploadImageToCloudinary(logoURL);
        String bannercode = uploadImageToCloudinary(bannerURL);
        // Tạo đối tượng Event từ request
        Event event = new Event();
        event.setEventName(request.getEventName());
        event.setLocation(request.getLocation());
        event.setTypeEvent(request.getTypeEvent());
        event.setDescription(request.getDescription());
        event.setCategory(category);
        event.setStatus("FENDING");
        event.setLogoURL(logocode);
        event.setBannerURL(bannercode);
        event.setOrganizer(organnizer);


        // Lưu vào database
        event = eventRepository.save(event);



        // Chuyển đổi sang DTO để trả về
        return modelMapper.map(event, EventDTO.class);
    }

    @Override
    public EventDTO updateEvent(UpdateEventRequest eventRequest, MultipartFile logoURL, MultipartFile bannerURL) throws IOException {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();
        var event = eventRepository.findEventByEventIdAndOrganizer_UserName(eventRequest.getEventId(),name)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        // Chỉ cập nhật nếu giá trị không null hoặc không phải chuỗi trống
        if (eventRequest.getEventName() != null && !eventRequest.getEventName().trim().isEmpty()) {
            event.setEventName(eventRequest.getEventName());
        }

        if (eventRequest.getDescription() != null && !eventRequest.getDescription().trim().isEmpty()) {
            event.setDescription(eventRequest.getDescription());
        }

        if (eventRequest.getLocation() != null) {
            event.setLocation(eventRequest.getLocation());
        }

        if (eventRequest.getStatus() != null) {
            event.setStatus(eventRequest.getStatus());
        }

        if (eventRequest.getTypeEvent() != null) {
            event.setTypeEvent(eventRequest.getTypeEvent());
        }

        if (eventRequest.getCategoryId() != 0) {
            var category = eventCategoryRepository.findById(eventRequest.getCategoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
            event.setCategory(category);
        }

        // Xử lý upload file nếu có
        if (logoURL != null && !logoURL.isEmpty()) {
            String logoUrl = uploadImageToCloudinary(logoURL);
            event.setLogoURL(logoUrl);
        }

        if (bannerURL != null && !bannerURL.isEmpty()) {
            String bannerUrl = uploadImageToCloudinary(bannerURL);
            event.setBannerURL(bannerUrl);
        }



        // Lưu thay đổi vào database
        event = eventRepository.save(event);

        // Chuyển đổi sang DTO và trả về
        return modelMapper.map(event,EventDTO.class);
    }

    @Override
    public boolean deleteEvent(int id) {
        var event = eventRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
        event.setStatus("REMOVE");
        return true;
    }

    @Override
    public List<EventDTO> getAllEvent() {
        List<Event> events = eventRepository.findAll();
        return modelMapper.map(events, new TypeToken<List<EventDTO>>() {}.getType());
    }

    @Override
    public EventDTO getEventById(int id) {
        var event = eventRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
        return modelMapper.map(event, EventDTO.class);
    }

    @Override
    public List<EventDTO> getEventByStatus(String status) {
        List<Event> events = eventRepository.findEventsByStatus(status)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
        return modelMapper.map(events, new TypeToken<List<EventDTO>>() {}.getType());
    }

    @Override
    public List<EventDTO> getEventByDraft() {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();
        var account = accountRepository.findAccountByUserName(name)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        List<Event> events = eventRepository.findEventsByStatusAndOrganizer_UserName("DRAFT",name)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
        return modelMapper.map(events, new TypeToken<List<EventDTO>>() {}.getType());
    }

    @Override
    public List<EventDTO> getEventByCompleted() {
        List<Event> events = eventRepository.findEventsByStatus("COMPLETED")
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
        return modelMapper.map(events, new TypeToken<List<EventDTO>>() {}.getType());
    }

    @Override
    public EventDTO approveEvent(int id) {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();
        var account = accountRepository.findAccountByUserName(name)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        if(!account.getRole().getRoleName().equals("MANAGER")){
            throw new AppException(ErrorCode.INVALID_ROLE);
        }

        var event = eventRepository.findEventByEventId(id)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        event.setStatus("APPROVED");
        eventRepository.save(event);
        return modelMapper.map(event, EventDTO.class);
    }

    @Override
    public List<EventDTO> getAllEventsByAccountId() {
        int uId = util.getAccountFromAuthentication().getAccountId();
        List<Event> events = eventRepository.findEventByOrganizerId(uId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
        return modelMapper.map(events, new TypeToken<List<EventDTO>>() {}.getType());
    }

    @Override
    public List<EventDTO> getEventsByAccountIdAndStatus(String status) {
        int uId = util.getAccountFromAuthentication().getAccountId();
        List<Event> events = eventRepository.findEventByOrganizerIdAndStatus(uId, status)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
        return modelMapper.map(events, new TypeToken<List<EventDTO>>() {}.getType());
    }

    private String uploadImageToCloudinary(MultipartFile file) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
        return (String) uploadResult.get("url");
    }


}
