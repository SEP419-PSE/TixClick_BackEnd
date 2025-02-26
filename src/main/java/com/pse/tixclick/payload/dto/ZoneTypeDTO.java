package com.pse.tixclick.payload.dto;

import com.pse.tixclick.payload.entity.entity_enum.EZoneType;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ZoneTypeDTO {
    private int typeId;

    private String typeName;
}
