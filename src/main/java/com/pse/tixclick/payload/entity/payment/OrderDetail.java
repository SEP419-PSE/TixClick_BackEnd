package com.pse.tixclick.payload.entity.payment;

import com.pse.tixclick.payload.entity.seatmap.Seat;
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
    private byte qrCode;

    @Column(nullable = false)
    private Date checkinTime;

    @Column(nullable = false)
    private String checkinStatus;

    @OneToOne
    @JoinColumn(name="seatId", nullable = false)
    private Seat seat;

    @ManyToOne
    @JoinColumn(name = "orderId", nullable = false)
    private Order order;
}
