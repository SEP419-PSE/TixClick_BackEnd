package com.pse.tixclick.payload.entity.event;

import com.pse.tixclick.payload.entity.Account;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Collection;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "events")
@Entity
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int eventId;

    @Column(columnDefinition = "NVARCHAR(255)")
    String eventName;

    @Column(columnDefinition = "NVARCHAR(255)")
    String location;

    @Column
    String status;

    @Column
    String typeEvent;

    @Column
    String logoURL;

    @Column
    String bannerURL;

    @Column
    String logoOrganizerURL;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    String description;

    @OneToMany(mappedBy = "event")
    Collection<EventActivity> eventActivities;

    @ManyToOne
    @JoinColumn(name = "organizer_id", nullable = false)
    Account organizer;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    EventCategory category;
}
