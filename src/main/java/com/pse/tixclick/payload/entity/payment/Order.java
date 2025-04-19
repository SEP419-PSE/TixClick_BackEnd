package com.pse.tixclick.payload.entity.payment;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pse.tixclick.payload.entity.Account;
import com.pse.tixclick.payload.entity.entity_enum.EOrderStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EOrderStatus status;

    @Column(nullable = false)
    private double totalAmount;

    @Column()
    private double totalAmountDiscount;

    @Column
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime orderDate;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    private Collection<OrderDetail> orderDetails;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    private Collection<Payment> payments;
}
