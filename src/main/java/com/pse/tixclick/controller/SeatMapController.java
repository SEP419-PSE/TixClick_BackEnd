package com.pse.tixclick.controller;

import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.payload.dto.EventActivityDTO;
import com.pse.tixclick.payload.dto.EventDTO;
import com.pse.tixclick.payload.dto.SeatMapDTO;
import com.pse.tixclick.payload.dto.TicketDTO;
import com.pse.tixclick.payload.request.SectionRequest;
import com.pse.tixclick.payload.request.create.CreateSeatMapRequest;
import com.pse.tixclick.payload.request.create.CreateTicketRequest;
import com.pse.tixclick.payload.request.update.UpdateSeatMapRequest;
import com.pse.tixclick.payload.response.ApiResponse;
import com.pse.tixclick.payload.response.GetSectionResponse;
import com.pse.tixclick.payload.response.SectionResponse;
import com.pse.tixclick.service.SeatMapService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/seat-map")
@Slf4j
@CrossOrigin(origins = "http://localhost:5173")
public class SeatMapController {
    @Autowired
    private SeatMapService seatMapService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<SeatMapDTO>> createSeatMap(@RequestBody CreateSeatMapRequest createSeatMapRequest) {
        try {
            SeatMapDTO seatMapDTO = seatMapService.createSeatMap(createSeatMapRequest);
            return ResponseEntity.ok(
                    ApiResponse.<SeatMapDTO>builder()
                            .code(200)
                            .message("Seat map created successfully")
                            .result(seatMapDTO)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body(ApiResponse.<SeatMapDTO>builder()
                            .code(400)
                            .message(e.getMessage())
                            .result(null)
                            .build());
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<SeatMapDTO>> updateSeatMap(@RequestBody UpdateSeatMapRequest updateSeatMapRequest, @PathVariable int id) {
        try {
            SeatMapDTO seatMapDTO = seatMapService.updateSeatMap(updateSeatMapRequest, id);
            return ResponseEntity.ok(
                    ApiResponse.<SeatMapDTO>builder()
                            .code(200)
                            .message("Seat map updated successfully")
                            .result(seatMapDTO)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body(ApiResponse.<SeatMapDTO>builder()
                            .code(400)
                            .message(e.getMessage())
                            .result(null)
                            .build());
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<String>> deleteSeatMap(@PathVariable int id) {
        try {
            seatMapService.deleteSeatMap(id);
            return ResponseEntity.ok(
                    ApiResponse.<String>builder()
                            .code(200)
                            .message("Seat map deleted successfully")
                            .result(null)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body(ApiResponse.<String>builder()
                            .code(400)
                            .message(e.getMessage())
                            .result(null)
                            .build());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<SeatMapDTO>>> getAllSeatMap() {
        List<SeatMapDTO> seatMapDTOS = seatMapService.getAllSeatMaps();

        if (seatMapDTOS.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<List<SeatMapDTO>>builder()
                            .code(HttpStatus.NOT_FOUND.value())
                            .message("No seatMaps found")
                            .result(null)
                            .build());
        }

        return ResponseEntity.ok(
                ApiResponse.<List<SeatMapDTO>>builder()
                        .code(HttpStatus.OK.value())
                        .message("Get all seatMaps successfully")
                        .result(seatMapDTOS)
                        .build()
        );
    }

    @PostMapping("/design-seatmap")
    public ResponseEntity<ApiResponse<List<SectionResponse>>> designZone(@RequestBody List<SectionRequest> sectionResponse, @RequestParam int eventId) {
        try {
            List<SectionResponse> sectionResponses = seatMapService.designZone(sectionResponse, eventId);
            return ResponseEntity.ok(
                    ApiResponse.<List<SectionResponse>>builder()
                            .code(200)
                            .message("Zone designed successfully")
                            .result(sectionResponses)
                            .build()
            );
        } catch (AppException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST.value())
                    .body(ApiResponse.<List<SectionResponse>>builder()
                            .code(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .result(null)
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST.value())
                    .body(ApiResponse.<List<SectionResponse>>builder()
                            .code(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .result(null)
                            .build());
        }
    }

    @GetMapping("/sections/{eventId}")
    public ResponseEntity<ApiResponse<List<SectionResponse>>> getSectionsByEventId(@PathVariable int eventId) {
        List<SectionResponse> sectionRequests = seatMapService.getSectionsByEventId(eventId);

        if (sectionRequests.isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(ApiResponse.<List<SectionResponse>>builder()
                            .code(HttpStatus.OK.value())
                            .message("No sections found")
                            .result(Collections.emptyList())
                            .build());
        }

        return ResponseEntity.ok(
                ApiResponse.<List<SectionResponse>>builder()
                        .code(HttpStatus.OK.value())
                        .message("Get all sections successfully")
                        .result(sectionRequests)
                        .build()
        );
    }

    @DeleteMapping("/zones/{zoneId}/events/{eventId}")
    public ResponseEntity<ApiResponse<List<SectionResponse>>> deleteZone(@PathVariable String zoneId, @PathVariable int eventId,@RequestBody List<SectionRequest> sectionResponse) {
        try {
            List<SectionResponse> sectionRequests = seatMapService.deleteZone(sectionResponse, zoneId, eventId);
            if(sectionRequests.isEmpty()) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(ApiResponse.<List<SectionResponse>>builder()
                                .code(HttpStatus.OK.value())
                                .message("No sections found")
                                .result(Collections.emptyList())
                                .build());
            }
            return ResponseEntity.ok(
                    ApiResponse.<List<SectionResponse>>builder()
                            .code(HttpStatus.OK.value())
                            .message("Zone deleted successfully")
                            .result(sectionRequests != null ? sectionRequests : Collections.emptyList())
                            .build()
            );
        } catch (AppException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.<List<SectionResponse>>builder()
                            .code(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .result(null)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST.value())
                    .body(ApiResponse.<List<SectionResponse>>builder()
                            .code(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .result(null)
                            .build());
        }
    }

    @GetMapping("/events/{eventId}/activities/{eventActivityId}")
    public ResponseEntity<ApiResponse<List<GetSectionResponse>>> getSections(@PathVariable int eventId, @PathVariable int eventActivityId) {
        List<GetSectionResponse> sectionRequests = seatMapService.getSections(eventId, eventActivityId);

        if (sectionRequests.isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(ApiResponse.<List<GetSectionResponse>>builder()
                            .code(HttpStatus.OK.value())
                            .message("No sections found")
                            .result(Collections.emptyList())
                            .build());
        }

        return ResponseEntity.ok(
                ApiResponse.<List<GetSectionResponse>>builder()
                        .code(HttpStatus.OK.value())
                        .message("Get all sections successfully")
                        .result(sectionRequests)
                        .build()
        );
    }

}