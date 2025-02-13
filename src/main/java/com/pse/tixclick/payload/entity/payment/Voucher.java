package com.pse.tixclick.payload.entity.payment;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Collection;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class Voucher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int voucherId;

    @Column(nullable = false)
    private String voucherName;

    @Column(nullable = false)
    private String voucherCode;

    @Column(nullable = false)
    private double discount;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private Date createAt;

    @Column(nullable = false)
    private int accountId;

    @OneToMany(mappedBy = "voucher")
    private Collection<OrderVoucher> orderVouchers;
}
