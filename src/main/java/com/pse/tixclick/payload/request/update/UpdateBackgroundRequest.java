package com.pse.tixclick.payload.request.update;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateBackgroundRequest {
    private String backgroundName;

    private String Type;

    private String value;
}
