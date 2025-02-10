package com.pse.tixclick.payload.entity.event;

import com.pse.tixclick.payload.entity.entity_enum.ETypeEvent;
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
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int eventId;

    @Column
    String eventName;

    @Column
    String location;

    @Column
    boolean status;

    @Column
    ETypeEvent typeEvent;

    @Column
    String description;

    @OneToMany(mappedBy = "event")
    Collection<EventActivity> eventActivities;

    @OneToMany(mappedBy = "event")
    Collection<EventImage> eventImages;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    EventCategory category;
}
