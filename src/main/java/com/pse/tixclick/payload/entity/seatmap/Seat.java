package com.pse.tixclick.payload.entity.seatmap;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pse.tixclick.payload.entity.payment.OrderDetail;
import com.pse.tixclick.payload.entity.ticket.Ticket;
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
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int seatId;

    @Column
    String rowNumber;

    @Column
    String columnNumber;

    @ManyToOne
    @JoinColumn(name = "zone_id", nullable = false)
    Zone zone;

    @Column
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdDate;

    @Column
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime updatedDate;

    @ManyToOne
    @JoinColumn(name="ticket_id", nullable = false)
    Ticket ticket;

    @OneToOne(mappedBy = "seat")
    private OrderDetail orderDetail;

    @Column
    boolean status;
}