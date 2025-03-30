package com.pse.tixclick.controller;

import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.payload.dto.CompanyDTO;
import com.pse.tixclick.payload.dto.ZoneDTO;
import com.pse.tixclick.payload.request.SectionRequest;
import com.pse.tixclick.payload.request.create.CreateZoneRequest;
import com.pse.tixclick.payload.request.update.UpdateZoneRequest;
import com.pse.tixclick.payload.response.ApiResponse;
import com.pse.tixclick.service.ZoneService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/zone")
@Slf4j
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ZoneController {
    @Autowired
    ZoneService zoneService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<ZoneDTO>> createZone(@RequestBody CreateZoneRequest createZoneRequest) {
        try {
            ZoneDTO zoneDTO = zoneService.createZone(createZoneRequest);
            return ResponseEntity.ok(
                    ApiResponse.<ZoneDTO>builder()
                            .code(200)
                            .message("Zone created successfully")
                            .result(zoneDTO)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body(ApiResponse.<ZoneDTO>builder()
                            .code(400)
                            .message(e.getMessage())
                            .result(null)
                            .build());
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<ZoneDTO>> updateZone(@RequestBody UpdateZoneRequest updateZoneRequest, @PathVariable int id) {
        try {
            ZoneDTO zoneDTO = zoneService.updateZone(updateZoneRequest, id);
            return ResponseEntity.ok(
                    ApiResponse.<ZoneDTO>builder()
                            .code(200)
                            .message("Zone updated successfully")
                            .result(zoneDTO)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body(ApiResponse.<ZoneDTO>builder()
                            .code(400)
                            .message(e.getMessage())
                            .result(null)
                            .build());
        }
    }



    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<ZoneDTO>>> getAllZones() {
        try {
            return ResponseEntity.ok(
                    ApiResponse.<List<ZoneDTO>>builder()
                            .code(200)
                            .message("Successfully fetched all zones")
                            .result(zoneService.getAllZones())
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body(ApiResponse.<List<ZoneDTO>>builder()
                            .code(400)
                            .message(e.getMessage())
                            .result(null)
                            .build());
        }
    }

    @GetMapping("/seat_map/{seatMapId}")
    public ResponseEntity<ApiResponse<List<ZoneDTO>>> getZonesBySeatMap(@PathVariable int seatMapId) {
        try {
            return ResponseEntity.ok(
                    ApiResponse.<List<ZoneDTO>>builder()
                            .code(200)
                            .message("Successfully fetched all zones")
                            .result(zoneService.getZonesBySeatMap(seatMapId))
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body(ApiResponse.<List<ZoneDTO>>builder()
                            .code(400)
                            .message(e.getMessage())
                            .result(null)
                            .build());
        }
    }

    @GetMapping("/type/{id}")
    public ResponseEntity<ApiResponse<List<ZoneDTO>>> getZonesByType(@PathVariable int id) {
        try {
            return ResponseEntity.ok(
                    ApiResponse.<List<ZoneDTO>>builder()
                            .code(200)
                            .message("Successfully fetched all zones")
                            .result(zoneService.getZonesByType(id))
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body(ApiResponse.<List<ZoneDTO>>builder()
                            .code(400)
                            .message(e.getMessage())
                            .result(null)
                            .build());
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<List<SectionRequest>>> deleteZone(@PathVariable int id) {
        try {
            List<SectionRequest> sectionRequests = zoneService.deleteZone(id);

            return ResponseEntity.ok(
                    ApiResponse.<List<SectionRequest>>builder()
                            .code(HttpStatus.OK.value())
                            .message("Zone deleted successfully")
                            .result(sectionRequests != null ? sectionRequests : Collections.emptyList())
                            .build()
            );
        } catch (AppException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.<List<SectionRequest>>builder()
                            .code(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .result(Collections.emptyList())
                            .build()
            );
        }
    }

}
