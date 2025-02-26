package com.pse.tixclick.payload.entity.payment;

import com.pse.tixclick.payload.entity.seatmap.Seat;
import com.pse.tixclick.payload.entity.ticket.Ticket;
import com.pse.tixclick.payload.entity.ticket.TicketPurchase;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class OrderDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int orderDetailId;

    @Column(nullable = false)
    private double amount;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @OneToOne
    @JoinColumn(name="ticket_purchase_id", nullable = false)
    private TicketPurchase ticketPurchase;

    @ManyToOne
    @JoinColumn(name = "voucher_id", nullable = true)
    private Voucher voucher;
}
