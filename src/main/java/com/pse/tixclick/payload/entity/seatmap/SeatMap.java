package com.pse.tixclick.payload.entity.seatmap;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pse.tixclick.payload.entity.event.Event;
import com.pse.tixclick.payload.entity.event.EventActivity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.Collection;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class SeatMap {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int seatMapId;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String seatMapName;

    @Column
    private String seatMapWidth;

    @Column
    private String seatMapHeight;

    @Column
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Column
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @Column
    private boolean status;

    @ManyToOne
    @JoinColumn(name = "background_id", nullable = false)
    private Background background;

    @OneToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @OneToMany(mappedBy = "seatMap")
    private Collection<Zone> zones;

    @OneToMany(mappedBy = "seatMap")
    private Collection<Icon> icons;
}
