package com.pse.tixclick.payload.request.create;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateBackgroundRequest {
    private String backgroundName;

    private String Type;

    private String value;
}
