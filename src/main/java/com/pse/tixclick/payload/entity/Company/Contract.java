package com.pse.tixclick.payload.entity.Company;

import com.pse.tixclick.payload.entity.Account;
import com.pse.tixclick.payload.entity.event.Event;
import com.pse.tixclick.payload.entity.payment.ContractPayment;
import com.pse.tixclick.payload.entity.payment.OrderDetail;
import com.pse.tixclick.payload.entity.payment.Payment;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Collection;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class Contract {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int contractId;

    @Column(nullable = false)
    private double totalAmount;

    @Column(nullable = false)
    private String commission;

    @Column(nullable = false)
    private String contractType;

    @Column(nullable = false)
    private String createdBy;

    @OneToOne
    @JoinColumn(name="customer_id", nullable = false)
    private Account account;

    @OneToOne
    @JoinColumn(name="event_id", nullable = false)
    private Event event;


}
