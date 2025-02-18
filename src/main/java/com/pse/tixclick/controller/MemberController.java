package com.pse.tixclick.controller;

import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.payload.request.CreateMemberRequest;
import com.pse.tixclick.payload.response.ApiResponse;
import com.pse.tixclick.payload.response.MemberDTOResponse;
import com.pse.tixclick.service.MemberService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/member")
@Slf4j
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MemberController {
    MemberService memberService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<MemberDTOResponse>> createMember(
            @RequestBody CreateMemberRequest createMemberRequest) {
        try {
            // Gọi service để tạo thành viên
            MemberDTOResponse createdMembers = memberService.createMember(createMemberRequest);

            // Kiểm tra xem có thành viên nào được tạo hay không
            boolean isMemberCreated = createdMembers != null && !createdMembers.getCreatedMembers().isEmpty();
            boolean isMailSent = createdMembers.getSentEmails() != null && !createdMembers.getSentEmails().isEmpty();

            String message = "An error occurred while processing the request.";

            if (isMemberCreated && isMailSent) {
                message = "Both member created and mail sent successfully";
            } else if (isMemberCreated) {
                message = "Member created successfully";
            } else if (isMailSent) {
                message = "Mail sent successfully";
            } else {
                message = "No members created or no emails sent"; // Trường hợp không có thành viên hoặc email nào được gửi
            }

            // Tạo response
            ApiResponse<MemberDTOResponse> response = ApiResponse.<MemberDTOResponse>builder()
                    .code(HttpStatus.CREATED.value())
                    .message(message)
                    .result(createdMembers)
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (AppException e) {
            ApiResponse<MemberDTOResponse> errorResponse = ApiResponse.<MemberDTOResponse>builder()
                    .code(HttpStatus.BAD_REQUEST.value())
                    .message(e.getMessage()) // Lỗi từ service
                    .result(null)
                    .build();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            ApiResponse<MemberDTOResponse> errorResponse = ApiResponse.<MemberDTOResponse>builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("An error occurred while processing the request.")
                    .result(null)
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Boolean>> deleteMember(@PathVariable int id) {
        try {
            // Gọi service để xóa thành viên
            boolean isDeleted = memberService.deleteMember(id);

            // Tạo response
            ApiResponse<Boolean> response = ApiResponse.<Boolean>builder()
                    .code(HttpStatus.OK.value())
                    .message("Member deleted successfully")
                    .result(isDeleted)
                    .build();

            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (AppException e) {
            ApiResponse<Boolean> errorResponse = ApiResponse.<Boolean>builder()
                    .code(HttpStatus.BAD_REQUEST.value())
                    .message(e.getMessage()) // Lỗi từ service
                    .result(null)
                    .build();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            ApiResponse<Boolean> errorResponse = ApiResponse.<Boolean>builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("An error occurred while processing the request.")
                    .result(null)
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

}
