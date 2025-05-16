package com.pse.tixclick.payload.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level =  lombok.AccessLevel.PRIVATE, makeFinal = false)

public class QrCodeRequest {
    String qrCode;
}
