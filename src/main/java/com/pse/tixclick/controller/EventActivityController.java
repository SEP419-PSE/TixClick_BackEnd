package com.pse.tixclick.controller;

import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.payload.dto.EventActivityDTO;
import com.pse.tixclick.payload.request.CreateEventActivityRequest;
import com.pse.tixclick.payload.response.ApiResponse;
import com.pse.tixclick.service.EventActivityService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/event-activity")
@Slf4j
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EventActivityController {
    EventActivityService eventActivityService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<EventActivityDTO>> createEventActivity(@RequestBody CreateEventActivityRequest eventActivityRequest) {
        try {
            EventActivityDTO eventActivityDTO = eventActivityService.createEventActivity(eventActivityRequest);
            return ResponseEntity.ok(
                    ApiResponse.<EventActivityDTO>builder()
                            .code(HttpStatus.OK.value())
                            .message("Event activity created successfully")
                            .result(eventActivityDTO)
                            .build()
            );
        } catch (AppException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST.value())
                    .body(ApiResponse.<EventActivityDTO>builder()
                            .code(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .result(null)
                            .build());
        }
    }

}
