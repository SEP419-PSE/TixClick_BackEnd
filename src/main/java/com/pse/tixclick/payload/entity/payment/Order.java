package com.pse.tixclick.payload.entity.payment;

import com.pse.tixclick.payload.entity.Account;
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
@Table(name = "orders")
public class  Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int orderId;

    @Column(nullable = false)
    private String orderCode;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private int totalAmount;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @OneToMany(mappedBy = "order")
    private Collection<OrderDetail> orderDetails;

    @OneToMany(mappedBy = "order")
    private Collection<Payment> payments;
}
