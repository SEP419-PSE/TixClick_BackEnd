package com.pse.tixclick.payload.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountDTO {
    int accountId;
    String firstName;
    String lastName;
    String userName;
    String email;
    String phone;
    String CCCD;
    String MSSV;
    boolean active;
    String avatarURL;
    String dob;
    int roleId;
}