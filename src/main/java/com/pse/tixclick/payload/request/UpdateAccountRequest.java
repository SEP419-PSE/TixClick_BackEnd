package com.pse.tixclick.payload.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

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
    String dob;
}