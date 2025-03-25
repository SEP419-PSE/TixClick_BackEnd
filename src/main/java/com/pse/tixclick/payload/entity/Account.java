package com.pse.tixclick.payload.entity;

import com.pse.tixclick.payload.entity.company.Company;
import com.pse.tixclick.payload.entity.company.Contract;
import com.pse.tixclick.payload.entity.payment.Order;
import com.pse.tixclick.payload.entity.payment.Payment;
import com.pse.tixclick.payload.entity.payment.Transaction;
import com.pse.tixclick.payload.entity.company.Member;
import com.pse.tixclick.payload.entity.payment.Voucher;
import com.pse.tixclick.payload.entity.ticket.Ticket;
import com.pse.tixclick.payload.entity.ticket.TicketPurchase;
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

    @Column
    String avatarURL;

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    Role role;

    @OneToMany(mappedBy = "account", fetch = FetchType.LAZY)
    private Collection<Member> members;

    @OneToMany(mappedBy = "account", fetch = FetchType.LAZY)
    private Collection<Order> orders;

    @OneToMany(mappedBy = "account", fetch = FetchType.LAZY)
    private Collection<Payment> payments;

    @OneToMany(mappedBy = "account", fetch = FetchType.LAZY)
    private Collection<Transaction> transactions;

    @OneToOne(mappedBy = "account", fetch = FetchType.LAZY)
    private Contract contract;

    @OneToMany(mappedBy = "account", fetch = FetchType.LAZY)
    private Collection<Voucher> vouchers;

    @OneToMany(mappedBy = "account", fetch = FetchType.LAZY)
    private Collection<Ticket> tickets;

    @OneToMany(mappedBy = "account", fetch = FetchType.LAZY)
    private Collection<TicketPurchase> ticketPurchases;

    @OneToOne(mappedBy = "representativeId", fetch = FetchType.LAZY) // ✅ mappedBy phải trùng tên field bên Company
    private Company company;


}