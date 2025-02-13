package com.pse.tixclick.payload.entity.payment;

import com.pse.tixclick.payload.entity.Account;
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
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int transactionId;

    @Column(nullable = false)
    private double amount;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private  String transactionCode;

    @Column(nullable = false)
    private Date transactionDate;

    @ManyToOne
    @JoinColumn(name = "orderId", nullable = false)
    private Order order;

    @ManyToOne
    @JoinColumn(name = "accountId", nullable = false)
    private Account account;
}
