package com.pse.tixclick.payload.entity.seatmap;

import com.pse.tixclick.payload.entity.entity_enum.EZoneType;
import com.pse.tixclick.payload.entity.entity_enum.ZoneTypeEnum;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class ZoneType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int typeId;

    @Enumerated(EnumType.STRING)
    @Column
    private ZoneTypeEnum typeName;
}
