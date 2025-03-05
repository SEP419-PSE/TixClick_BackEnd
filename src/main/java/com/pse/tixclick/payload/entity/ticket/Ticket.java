package com.pse.tixclick.payload.entity.ticket;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pse.tixclick.payload.entity.Account;
import com.pse.tixclick.payload.entity.event.EventActivity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int ticketId;

    @Column(columnDefinition = "NVARCHAR(255)")
    String ticketName;

    @Column
    int quantity;

    @Column
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdDate;

    @Column
    double price;

    @Column
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate startDate;

    @Column
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate endDate;

    @Column
    int minQuantity;

    @Column
    int maxQuantity;

    @Column
    String seatBackgroundColor;

    @Column
    String textColor;

    @Column
    boolean status;

    @ManyToOne
    @JoinColumn(name="account_id", nullable = false)
    Account account;

    @OneToOne
    @JoinColumn(name="event_activity_id", nullable = false)
    EventActivity eventActivity;


}
