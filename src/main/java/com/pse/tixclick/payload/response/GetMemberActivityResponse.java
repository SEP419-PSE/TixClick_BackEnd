package com.pse.tixclick.payload.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class GetMemberActivityResponse {
    GetMemberResponse member;
    int memberActivityId;
    String status;
    int eventActivityId;

}
