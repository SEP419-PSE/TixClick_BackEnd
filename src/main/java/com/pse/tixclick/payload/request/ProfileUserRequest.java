package com.pse.tixclick.payload.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level =  lombok.AccessLevel.PRIVATE, makeFinal = false)
public class ProfileUserRequest {
    String userId;
    String address;
    String phone;
    String avatar;
}
