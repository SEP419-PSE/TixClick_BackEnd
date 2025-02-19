package com.pse.tixclick.controller;

import com.pse.tixclick.payload.dto.BackgroundDTO;
import com.pse.tixclick.payload.request.create.CreateBackgroundRequest;
import com.pse.tixclick.payload.request.update.UpdateBackgroundRequest;
import com.pse.tixclick.payload.response.ApiResponse;
import com.pse.tixclick.service.BackgroundService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/background")
@Slf4j
@CrossOrigin(origins = "http://localhost:5173")
public class BackgroundController {
    @Autowired
    private BackgroundService backgroundService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<BackgroundDTO>> createBackground(@RequestBody CreateBackgroundRequest createBackgroundRequest) {
        try {
            BackgroundDTO backgroundDTO = backgroundService.createBackground(createBackgroundRequest);
            return ResponseEntity.ok(
                    ApiResponse.<BackgroundDTO>builder()
                            .code(200)
                            .message("Background created successfully")
                            .result(backgroundDTO)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body(ApiResponse.<BackgroundDTO>builder()
                            .code(400)
                            .message(e.getMessage())
                            .result(null)
                            .build());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<BackgroundDTO>>> getAllBackground() {
        List<BackgroundDTO> backgroundDTOS = backgroundService.getAllBackgrounds();

        if (backgroundDTOS.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<List<BackgroundDTO>>builder()
                            .code(HttpStatus.NOT_FOUND.value())
                            .message("No Backgrounds found")
                            .result(null)
                            .build());
        }

        return ResponseEntity.ok(
                ApiResponse.<List<BackgroundDTO>>builder()
                        .code(HttpStatus.OK.value())
                        .message("Get all Backgrounds successfully")
                        .result(backgroundDTOS)
                        .build()
        );
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<BackgroundDTO>> updateBackground(@RequestBody UpdateBackgroundRequest updateBackgroundRequest, @PathVariable int id) {
        try {
            BackgroundDTO backgroundDTO = backgroundService.updateBackground(updateBackgroundRequest, id);
            return ResponseEntity.ok(
                    ApiResponse.<BackgroundDTO>builder()
                            .code(200)
                            .message("Background updated successfully")
                            .result(backgroundDTO)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body(ApiResponse.<BackgroundDTO>builder()
                            .code(400)
                            .message(e.getMessage())
                            .result(null)
                            .build());
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<String>> deleteBackground(@PathVariable int id) {
        try {
            backgroundService.deleteBackground(id);
            return ResponseEntity.ok(
                    ApiResponse.<String>builder()
                            .code(200)
                            .message("Background deleted successfully")
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
}
