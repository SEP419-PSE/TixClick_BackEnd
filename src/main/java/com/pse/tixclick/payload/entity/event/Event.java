package com.pse.tixclick.payload.entity.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pse.tixclick.payload.entity.Account;
import com.pse.tixclick.payload.entity.company.Company;
import com.pse.tixclick.payload.entity.company.Contract;
import com.pse.tixclick.payload.entity.entity_enum.EEventStatus;
import com.pse.tixclick.payload.entity.seatmap.SeatMap;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
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
    String locationName;

    @Column(columnDefinition = "NVARCHAR(255)")
    String location;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    EEventStatus status;

    @Column
    String typeEvent;

    @Column
    int countView;

    @Column
    String logoURL;

    @Column
    String bannerURL;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    String description;

    @Column
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate startDate;

    @Column
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate endDate;

    @Column
    String UrlOnline;

    @OneToMany(mappedBy = "event")
    Collection<EventActivity> eventActivities;

    @ManyToOne
    @JoinColumn(name = "organizer_id", nullable = false)
    Account organizer;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    EventCategory category;

    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    Company company;

    @OneToOne(mappedBy = "event", fetch = FetchType.LAZY)
    private Contract contract;

    @OneToOne(mappedBy = "event", fetch = FetchType.LAZY)
    private SeatMap seatMap;
}
