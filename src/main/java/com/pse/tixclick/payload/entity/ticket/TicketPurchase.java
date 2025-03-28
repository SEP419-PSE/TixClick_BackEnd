package com.pse.tixclick.payload.entity.ticket;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pse.tixclick.payload.entity.Account;
import com.pse.tixclick.payload.entity.event.Event;
import com.pse.tixclick.payload.entity.event.EventActivity;
import com.pse.tixclick.payload.entity.payment.OrderDetail;
import com.pse.tixclick.payload.entity.seatmap.ActivityAssignment;
import com.pse.tixclick.payload.entity.seatmap.Seat;
import com.pse.tixclick.payload.entity.seatmap.Zone;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
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

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String qrCode;

    @Column(nullable = false)
    private String status;

    @Column
    private Integer quantity;

    @ManyToOne
    @JoinColumn(name="ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @OneToMany(mappedBy = "ticketPurchase")
    private Collection<OrderDetail> orderDetails;

    @OneToMany(mappedBy = "ticketPurchase")
    private Collection<ActivityAssignment> activityAssignments;

}
