package com.pse.tixclick.payload.entity.seatmap;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pse.tixclick.payload.entity.payment.OrderDetail;
import com.pse.tixclick.payload.entity.ticket.Ticket;
import com.pse.tixclick.payload.entity.ticket.TicketPurchase;
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
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int seatId;

    @Column
    private String rowNumber;

    @Column
    private String columnNumber;

    @Column
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdDate;

    @Column
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedDate;

    @ManyToOne
    @JoinColumn(name="zone_id", nullable = false)
    private Zone zone;

    @ManyToOne
    @JoinColumn(name="ticket_id", nullable = false)
    private Ticket ticket;

    @OneToMany(mappedBy = "seat")
    private Collection<SeatActivity> seatActivities;
}