package com.pse.tixclick.payload.request.update;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateAccountRequest {
    String firstName;
    String lastName;
    String email;
    String phone;
    LocalDate dob;
    String CCCD;
    String MSSV;
    MultipartFile avatarURL;
}