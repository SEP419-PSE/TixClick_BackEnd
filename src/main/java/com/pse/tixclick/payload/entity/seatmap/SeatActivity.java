package com.pse.tixclick.payload.entity.seatmap;

import com.pse.tixclick.payload.entity.event.EventActivity;
import com.pse.tixclick.payload.entity.ticket.TicketPurchase;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Collection;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "seat_activity")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class SeatActivity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int seatActivityId;

    @Column
    private String status;

    @ManyToOne
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @ManyToOne
    @JoinColumn(name = "zone_activity_id", nullable = false)
    private ZoneActivity zoneActivity;

    @ManyToOne
    @JoinColumn(name = "event_activity_id", nullable = false)
    private EventActivity eventActivity;

    @OneToMany(mappedBy = "seatActivity")
    private Collection<TicketPurchase> ticketPurchases;
}

