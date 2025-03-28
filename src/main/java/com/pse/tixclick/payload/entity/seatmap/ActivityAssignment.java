package com.pse.tixclick.payload.entity.seatmap;

import com.pse.tixclick.payload.entity.event.EventActivity;
import com.pse.tixclick.payload.entity.ticket.TicketPurchase;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "activity_assignments")
public class ActivityAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int activityAssignmentId;

    @Column
    private String status;

    @ManyToOne
    @JoinColumn(name = "event_activity_id", nullable = false)
    private EventActivity eventActivity;

    @ManyToOne
    @JoinColumn(name = "seat_activity_id")
    private SeatActivity seatActivity;

    @ManyToOne
    @JoinColumn(name = "zone_activity_id")
    private ZoneActivity zoneActivity;

    @ManyToOne
    @JoinColumn(name = "ticket_purchase_id", nullable = false)
    private TicketPurchase ticketPurchase;
}
