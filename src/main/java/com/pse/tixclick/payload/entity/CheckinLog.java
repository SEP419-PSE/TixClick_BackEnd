package com.pse.tixclick.payload.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pse.tixclick.payload.entity.entity_enum.ECheckinLogStatus;
import com.pse.tixclick.payload.entity.ticket.TicketPurchase;
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
public class CheckinLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int checkinId;

    @Column
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkinTime;

    @Column
    private String checkinDevice;

    @Column
    private String checkinLocation;

    @Enumerated(EnumType.STRING)
    @Column
    private ECheckinLogStatus checkinStatus;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    @ManyToOne
    @JoinColumn(name = "ticket_purchase_id")
    private TicketPurchase ticketPurchase;
}
