package com.pse.tixclick.payload.entity.seatmap;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class Background {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int backgroundId;

    @Column(columnDefinition = "NVARCHAR(255)")
    String backgroundName;

    @Column
    String Type;

    @Column
    String value;

}
