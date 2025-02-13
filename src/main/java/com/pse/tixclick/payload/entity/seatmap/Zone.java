package com.pse.tixclick.payload.entity.seatmap;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "zone")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class Zone {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int zoneId;

    @Column(columnDefinition = "NVARCHAR(255)")
    String zoneName;

    @ManyToOne
    @JoinColumn(name = "seatmap_id", nullable = false)
    SeatMap seatMap;

    @Column
    String xPosition;

    @Column
    String yPosition;

    @Column
    String width;

    @Column
    String height;

    @Column
    int quantity;

    @Column
    int availableQuantity;

    @Column
    String rows;

    @Column
    String columns;

    @ManyToOne
    @JoinColumn(name = "zone_type_id", nullable = false)
    ZoneType zoneType;

    @Column
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdDate;

    @Column
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime updatedDate;

    @Column
    boolean status;

}
