package com.pse.tixclick.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.exception.ErrorCode;
import com.pse.tixclick.payload.dto.EventDTO;
import com.pse.tixclick.payload.entity.entity_enum.ETypeEvent;
import com.pse.tixclick.payload.entity.event.Event;
import com.pse.tixclick.payload.request.CreateEventRequest;
import com.pse.tixclick.payload.request.UpdateEventRequest;
import com.pse.tixclick.repository.EventCategoryRepository;
import com.pse.tixclick.repository.EventRepository;
import com.pse.tixclick.service.EventService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EventServiceImpl implements EventService {
    EventRepository eventRepository;
    ModelMapper modelMapper;
    EventCategoryRepository eventCategoryRepository;
    Cloudinary cloudinary;
    @Override
    public EventDTO createEvent(CreateEventRequest request, MultipartFile logoURL, MultipartFile bannerURL, MultipartFile logoOrganizeURL) throws IOException {
        if (request == null || request.getEventName() == null || request.getCategoryId() == 0) {
            throw new AppException(ErrorCode.INVALID_EVENT_DATA);
        }

        var category = eventCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        // Upload từng ảnh lên Cloudinary
        String logocode = uploadImageToCloudinary(logoURL);
        String bannercode = uploadImageToCloudinary(bannerURL);
        String logoOrganizercode = uploadImageToCloudinary(logoOrganizeURL);

        // Tạo đối tượng Event từ request
        Event event = new Event();
        event.setEventName(request.getEventName());
        event.setLocation(request.getLocation());
        event.setTypeEvent(request.getTypeEvent());
        event.setDescription(request.getDescription());
        event.setCategory(category);
        event.setStatus(false);
        event.setLogoURL(logocode);
        event.setBannerURL(bannercode);
        event.setLogoOrganizerURL(logoOrganizercode);


        // Lưu vào database
        event = eventRepository.save(event);



        // Chuyển đổi sang DTO để trả về
        return modelMapper.map(event, EventDTO.class);
    }

    @Override
    public EventDTO updateEvent(UpdateEventRequest eventRequest, MultipartFile logoURL, MultipartFile bannerURL, MultipartFile logoOrganizeURL) throws IOException {
        var event = eventRepository.findById(eventRequest.getEventId())
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

        event.setStatus(eventRequest.isStatus());

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

        if (logoOrganizeURL != null && !logoOrganizeURL.isEmpty()) {
            String logoOrganizeUrl = uploadImageToCloudinary(logoOrganizeURL);
            event.setLogoOrganizerURL(logoOrganizeUrl);
        }

        // Lưu thay đổi vào database
        event = eventRepository.save(event);

        // Chuyển đổi sang DTO và trả về
        return modelMapper.map(event,EventDTO.class);
    }



    private String uploadImageToCloudinary(MultipartFile file) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
        return (String) uploadResult.get("url");
    }

}
