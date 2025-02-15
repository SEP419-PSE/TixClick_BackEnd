package com.pse.tixclick.payload.entity.ticket;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pse.tixclick.payload.entity.event.EventActivity;
import com.pse.tixclick.payload.entity.payment.OrderDetail;
import com.pse.tixclick.payload.entity.seatmap.Seat;
import com.pse.tixclick.payload.entity.seatmap.Zone;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class TicketPurchase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int ticketId;

    @Column(nullable = false)
    private Byte qrCode;

    @Column
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime checkInTime;

    @Column
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime checkOutTime;

    @Column(nullable = false)
    private String status;

    @ManyToOne
    @JoinColumn(name="zone_id", nullable = false)
    private Zone zone;

    @OneToOne
    @JoinColumn(name="seat_id", nullable = false)
    private Seat seat;

    @ManyToOne
    @JoinColumn(name="event_activity_id", nullable = false)
    private EventActivity eventActivity;

    @OneToOne
    @JoinColumn(name="ticket_id", nullable = false)
    private Ticket ticket;

    @OneToOne(mappedBy = "ticketPurchase")
    private OrderDetail orderDetail;
}
