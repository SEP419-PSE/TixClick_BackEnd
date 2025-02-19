package com.pse.tixclick.payload.entity.seatmap;

import com.fasterxml.jackson.annotation.JsonFormat;
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
@Table(name = "zone")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class Zone {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int zoneId;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String zoneName;

    @Column
    private String xPosition;

    @Column
    private String yPosition;

    @Column
    private String width;

    @Column
    private String height;

    @Column
    private int quantity;

    @Column
    private int availableQuantity;

    @Column
    private String rows;

    @Column
    private String columns;

    @Column
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdDate;

    @Column
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedDate;

    @Column
    private boolean status;

    @ManyToOne
    @JoinColumn(name = "seatmap_id", nullable = false)
    private SeatMap seatMap;

    @ManyToOne
    @JoinColumn(name = "zone_type_id", nullable = false)
    private ZoneType zoneType;

    @OneToMany(mappedBy = "zone")
    private Collection<TicketPurchase> ticketPurchases;

    @OneToMany(mappedBy = "zone")
    private Collection<Seat> seats;
}
