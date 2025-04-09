package com.pse.tixclick.controller;

import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.payload.dto.MemberActivityDTO;
import com.pse.tixclick.payload.request.create.CreateMemberActivityRequest;
import com.pse.tixclick.payload.response.ApiResponse;
import com.pse.tixclick.payload.response.BulkMemberActivityResult;
import com.pse.tixclick.payload.response.GetMemberActivityResponse;
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
    public ResponseEntity<ApiResponse<BulkMemberActivityResult>> addMemberActivities(
            @RequestBody CreateMemberActivityRequest request) {
        try {
            BulkMemberActivityResult result = memberActivityService.addMemberActivities(request);
            return ResponseEntity.ok(
                    ApiResponse.<BulkMemberActivityResult>builder()
                            .code(HttpStatus.OK.value())
                            .message("Member activities added successfully")
                            .result(result)
                            .build()
            );
        } catch (AppException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<BulkMemberActivityResult>builder()
                            .code(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .result(null)
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<BulkMemberActivityResult>builder()
                            .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("Internal Server Error: " + e.getMessage())
                            .result(null)
                            .build());
        }
    }


    @GetMapping("/{eventActivityId}")
    public ResponseEntity<ApiResponse<List<GetMemberActivityResponse>>> getMemberActivitiesByEventActivityId(
            @PathVariable int eventActivityId) {
        try {
            List<GetMemberActivityResponse> result = memberActivityService.getMemberActivitiesByEventActivityId(eventActivityId);
            return ResponseEntity.ok(
                    ApiResponse.<List<GetMemberActivityResponse>>builder()
                            .code(HttpStatus.OK.value())
                            .message("Member activities retrieved successfully")
                            .result(result)
                            .build()
            );
        } catch (AppException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<List<GetMemberActivityResponse>>builder()
                            .code(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .result(null)
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<GetMemberActivityResponse>>builder()
                            .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("Internal Server Error: " + e.getMessage())
                            .result(null)
                            .build());
        }
    }

    @DeleteMapping("/{memberActivityId}/{companyId}")
    public ResponseEntity<ApiResponse<Boolean>> deleteMemberActivity(
            @PathVariable int memberActivityId,
            @PathVariable int companyId) {
        try {
            boolean result = memberActivityService.deleteMemberActivity(memberActivityId, companyId);
            return ResponseEntity.ok(
                    ApiResponse.<Boolean>builder()
                            .code(HttpStatus.OK.value())
                            .message("Member activity deleted successfully")
                            .result(result)
                            .build()
            );
        } catch (AppException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<Boolean>builder()
                            .code(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .result(null)
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Boolean>builder()
                            .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("Internal Server Error: " + e.getMessage())
                            .result(null)
                            .build());
        }
    }
}
