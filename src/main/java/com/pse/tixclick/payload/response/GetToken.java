package com.pse.tixclick.payload.response;

import com.pse.tixclick.payload.entity.entity_enum.ERole;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GetToken {
    ERole roleName;
    String userName;
}
