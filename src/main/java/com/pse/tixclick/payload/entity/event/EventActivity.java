package com.pse.tixclick.payload.entity.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pse.tixclick.payload.entity.Account;
import com.pse.tixclick.payload.entity.seatmap.SeatActivity;
import com.pse.tixclick.payload.entity.seatmap.SeatMap;
import com.pse.tixclick.payload.entity.seatmap.ZoneActivity;
import com.pse.tixclick.payload.entity.ticket.Ticket;
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

    @Column(columnDefinition = "NVARCHAR(255)", unique = true)
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

    @OneToMany(mappedBy = "eventActivity")
    Collection<TicketPurchase> ticketPurchases;

    @OneToMany(mappedBy = "eventActivity")
    Collection<ZoneActivity> zoneActivities;

    @OneToMany(mappedBy = "eventActivity")
    Collection<SeatActivity> seatActivities;
}
