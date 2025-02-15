package com.pse.tixclick.payload.entity.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pse.tixclick.payload.entity.Account;
import com.pse.tixclick.payload.entity.ticket.Ticket;
import com.pse.tixclick.payload.entity.ticket.TicketPurchase;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class EventActivity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int eventActivityId;

    @Column(columnDefinition = "NVARCHAR(255)")
    String activityName;

    @Column
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate date;

    @Column
    @JsonFormat(pattern = "HH:mm:ss")
    LocalTime startTime;

    @Column
    @JsonFormat(pattern = "HH:mm:ss")
    LocalTime endTime;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    Event event;

    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    Account createdBy;

    @OneToMany(mappedBy = "eventActivity")
    private Collection<TicketPurchase> ticketPurchases;

    @OneToOne(mappedBy = "eventActivity")
    private Ticket ticket;
}
