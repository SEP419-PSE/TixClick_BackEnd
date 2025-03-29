package com.pse.tixclick.payload.request.create;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateTickeSeatMaptRequest {
    String id;
    String name;
    String color;
    String textColor;
    double price;
    int minQuantity;
    int maxQuantity;
    int eventId;
}
