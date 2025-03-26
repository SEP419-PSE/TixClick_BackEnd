package com.pse.tixclick.payload.entity.ticket;

import com.pse.tixclick.payload.entity.event.EventActivity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class TicketMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int ticketMappingId;

    @ManyToOne
    @JoinColumn(name = "ticket_id", nullable = false)
    Ticket ticket;

    @ManyToOne
    @JoinColumn(name = "event_activity_id", nullable = false)
    EventActivity eventActivity;

    @Column
    int quantity;

    @Column
    boolean status;

}
