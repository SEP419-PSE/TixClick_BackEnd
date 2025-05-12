package com.pse.tixclick.payload.request;

import com.pse.tixclick.payload.entity.entity_enum.ERole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SignUpRequest {

    @NotBlank(message = "Username không được để trống")
    @Size(min = 6, max = 20, message = "Username phải từ 6 đến 20 ký tự")
    String userName;

    @NotBlank(message = "Password không được để trống")
    String password;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    String email;

    @NotBlank(message = "First name không được để trống")
    String firstName;

    @NotBlank(message = "Last name không được để trống")
    String lastName;

    ERole roleName;
}
