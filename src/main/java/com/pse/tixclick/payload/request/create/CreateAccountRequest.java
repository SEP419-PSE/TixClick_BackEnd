package com.pse.tixclick.payload.request.create;

import com.pse.tixclick.payload.entity.entity_enum.ERole;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateAccountRequest {
    String email;
    String username;
    ERole role;
}
