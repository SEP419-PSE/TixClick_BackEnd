package com.pse.tixclick.payload.entity.ticket;

import com.fasterxml.jackson.annotation.JsonFormat;
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
public class TicketPricing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int ticketPricingId;

    @Column
    double price;

    @Column
    String currency;

    @Column
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime startDate;

    @Column
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime endDate;

    @ManyToOne
    @JoinColumn(name="ticket_id", nullable = false)
    Ticket ticket;

    @Column
    boolean status;

}
