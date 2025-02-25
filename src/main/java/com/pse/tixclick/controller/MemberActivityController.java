package com.pse.tixclick.controller;

import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.payload.dto.MemberActivityDTO;
import com.pse.tixclick.payload.request.create.CreateMemberActivityRequest;
import com.pse.tixclick.payload.response.ApiResponse;
import com.pse.tixclick.service.MemberActivityService;
import com.pse.tixclick.service.MemberService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/member-activity")
@Slf4j
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MemberActivityController {
    MemberActivityService memberActivityService;

    @PostMapping
    public ResponseEntity<ApiResponse<List<MemberActivityDTO>>> addMemberActivities(
            @RequestBody CreateMemberActivityRequest request) {
        try {
            List<MemberActivityDTO> result = memberActivityService.addMemberActivities(request);
            return ResponseEntity.ok(
                    ApiResponse.<List<MemberActivityDTO>>builder()
                            .code(HttpStatus.OK.value())
                            .message("Member activities added successfully")
                            .result(result)
                            .build()
            );
        } catch (AppException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<List<MemberActivityDTO>>builder()
                            .code(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .result(null)
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<MemberActivityDTO>>builder()
                            .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("Internal Server Error: " + e.getMessage())
                            .result(null)
                            .build());
        }
    }

}
