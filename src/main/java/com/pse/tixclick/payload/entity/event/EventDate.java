package com.pse.tixclick.payload.entity.event;

import com.pse.tixclick.payload.entity.event.Event;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.*;

import java.time.LocalTime;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class EventDate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int eventDateId;

    @Column
    Date date;

    @Column
    LocalTime startTime;

    @Column
    LocalTime endTime;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    Event event;
}
