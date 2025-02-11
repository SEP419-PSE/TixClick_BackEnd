package com.pse.tixclick.payload.entity.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class EventActivity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int eventActivityId;

    @Column(columnDefinition = "NVARCHAR(255)")
    String activityName;

    @Column
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate date;

    @Column
    @JsonFormat(pattern = "HH:mm:ss")
    LocalTime startTime;

    @Column
    @JsonFormat(pattern = "HH:mm:ss")
    LocalTime endTime;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    Event event;
}
