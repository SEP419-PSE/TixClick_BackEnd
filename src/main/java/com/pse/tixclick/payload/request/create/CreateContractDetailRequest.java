package com.pse.tixclick.payload.request.create;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateContractDetailRequest {
    String contractDetailName;
    String contractDetailCode;
    String contractDetailDescription;
    double contractDetailAmount;
    LocalDate contractDetailPayDate;
    double contractDetailPercentage;
}
