package com.pse.tixclick.payload.entity.seatmap;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pse.tixclick.payload.entity.event.Event;
import com.pse.tixclick.payload.entity.event.EventActivity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

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
    int seatMapId;

    @Column(columnDefinition = "NVARCHAR(255)")
    String seatMapName;

    @Column
    String seatMapWidth;

    @Column
    String seatMapHeight;

    @Column
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    String createdAt;

    @Column
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    String updatedAt;

    @Column
    boolean status;

    @ManyToOne
    @JoinColumn(name = "background_id", nullable = false)
    Background background;

    @OneToOne
    @JoinColumn(name = "event_activity_id", nullable = false)
    EventActivity eventActivity;

    @OneToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;
}
