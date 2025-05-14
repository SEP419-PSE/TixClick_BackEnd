package com.pse.tixclick.payload.entity.ticket;

import com.pse.tixclick.payload.entity.Account;
import com.pse.tixclick.payload.entity.entity_enum.ETicketPurchaseStatus;
import com.pse.tixclick.payload.entity.event.Event;
import com.pse.tixclick.payload.entity.event.EventActivity;
import com.pse.tixclick.payload.entity.payment.OrderDetail;
import com.pse.tixclick.payload.entity.seatmap.*;
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
public class TicketPurchase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int ticketPurchaseId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ETicketPurchaseStatus status;

    @Column
    private Integer quantity;

    @ManyToOne
    @JoinColumn(name = "seat_activity_id")
    private SeatActivity seatActivity;

    @ManyToOne
    @JoinColumn(name = "zone_activity_id")
    private ZoneActivity zoneActivity;

    @Column
    private Integer ticketPurchaseOldId;

    @ManyToOne
    @JoinColumn(name="ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne
    @JoinColumn(name = "event_activity_id", nullable = false)
    private EventActivity eventActivity;

    @OneToMany(mappedBy = "ticketPurchase", fetch = FetchType.LAZY)
    private Collection<OrderDetail> orderDetails;
}
