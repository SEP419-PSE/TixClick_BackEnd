package com.pse.tixclick.payload.entity.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pse.tixclick.payload.entity.Account;
import com.pse.tixclick.payload.entity.seatmap.SeatActivity;
import com.pse.tixclick.payload.entity.seatmap.SeatMap;
import com.pse.tixclick.payload.entity.seatmap.ZoneActivity;
import com.pse.tixclick.payload.entity.ticket.Ticket;
import com.pse.tixclick.payload.entity.ticket.TicketMapping;
import com.pse.tixclick.payload.entity.ticket.TicketPurchase;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Builder
public class EventActivity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int eventActivityId;

    @Column(columnDefinition = "NVARCHAR(255)")
    String activityName;

    @Column
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate dateEvent;

    @Column
    @JsonFormat(pattern = "HH:mm:ss")
    LocalTime startTimeEvent;

    @Column
    @JsonFormat(pattern = "HH:mm:ss")
    LocalTime endTimeEvent;

    @Column
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime startTicketSale;

    @Column
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime endTicketSale;

    @ManyToOne
    @JoinColumn(name = "seatmap_id")
    SeatMap seatMap;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    Event event;

    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    Account createdBy;

    @OneToMany(mappedBy = "eventActivity", fetch = FetchType.LAZY)
    Collection<TicketPurchase> ticketPurchases;

    @OneToMany(mappedBy = "eventActivity", fetch = FetchType.LAZY)
    Collection<ZoneActivity> zoneActivities;

    @OneToMany(mappedBy = "eventActivity", fetch = FetchType.LAZY)
    Collection<SeatActivity> seatActivities;

    @OneToMany(mappedBy = "eventActivity", fetch = FetchType.LAZY)
    Collection<TicketMapping> tickets;
}
