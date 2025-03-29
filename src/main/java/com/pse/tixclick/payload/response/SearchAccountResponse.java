package com.pse.tixclick.payload.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level =  lombok.AccessLevel.PRIVATE, makeFinal = false)
public class SearchAccountResponse {
    String userName;
    String email;
    String firstName;
    String lastName;
    String avatar;

}
