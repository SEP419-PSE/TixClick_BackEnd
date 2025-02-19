package com.pse.tixclick.payload.entity.seatmap;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.*;

import java.util.Collection;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class Background {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int backgroundId;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String backgroundName;

    @Column
    private String Type;

    @Column
    private String value;

    @OneToMany(mappedBy = "background")
    private Collection<SeatMap> seatMaps;
}
