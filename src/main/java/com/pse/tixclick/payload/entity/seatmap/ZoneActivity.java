package com.pse.tixclick.payload.entity.seatmap;

import com.pse.tixclick.payload.entity.event.EventActivity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Collection;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "zone_activity")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class ZoneActivity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int zoneActivityId;

    @Column
    private int availableQuantity;

    @ManyToOne
    @JoinColumn(name = "zone_id", nullable = false)
    private Zone zone;

    @ManyToOne
    @JoinColumn(name = "event_activity_id", nullable = false)
    private EventActivity eventActivity;

    @OneToMany(mappedBy = "zoneActivity")
    private Collection<ActivityAssignment> activityAssignments;

    @OneToMany(mappedBy = "zoneActivity")
    private Collection<SeatActivity> seatActivities;
}
