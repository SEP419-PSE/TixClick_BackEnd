package com.pse.tixclick.payload.entity;

import com.pse.tixclick.payload.entity.Company.Contract;
import com.pse.tixclick.payload.entity.payment.Order;
import com.pse.tixclick.payload.entity.payment.Payment;
import com.pse.tixclick.payload.entity.payment.Transaction;
import com.pse.tixclick.payload.entity.Company.Member;
import com.pse.tixclick.payload.entity.payment.Voucher;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Collection;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int accountId;

    @Column(columnDefinition = "NVARCHAR(255)")
    String firstName;

    @Column(columnDefinition = "NVARCHAR(255)")
    String lastName;

    @Column(unique = true, nullable = false, length = 50)
    String userName;

    @Column(nullable = false)
    String password;

    @Column(unique = true, nullable = false, length = 100)
    String email;

    @Column(columnDefinition = "VARCHAR(20)")
    String phone;

    @Column
    boolean active;

    @Column
    LocalDate dob;

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    Role role;

    @OneToMany(mappedBy = "account")
    Collection<Member> members;

    @OneToMany(mappedBy = "account")
    Collection<Order> orders;

    @OneToMany(mappedBy = "account")
    Collection<Payment> payments;

    @OneToMany(mappedBy = "account")
    Collection<Transaction> transactions;

    @OneToOne(mappedBy = "account")
    private Contract contract;

    @OneToMany(mappedBy = "account")
    private Collection<Voucher> vouchers;
}
