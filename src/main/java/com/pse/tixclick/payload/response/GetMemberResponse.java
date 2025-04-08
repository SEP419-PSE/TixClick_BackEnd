package com.pse.tixclick.payload.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GetMemberResponse {
    int memberId;
    String subRole;
    String userName;
    String email;
    String phoneNumber;
    String lastName;
    String firstName;
    String status;




}

