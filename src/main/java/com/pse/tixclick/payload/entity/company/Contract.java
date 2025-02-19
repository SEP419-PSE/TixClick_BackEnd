package com.pse.tixclick.payload.entity.company;

import com.pse.tixclick.payload.entity.Account;
import com.pse.tixclick.payload.entity.event.Event;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

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

    @OneToOne
    @JoinColumn(name="manager_id", nullable = false)
    private Account account;

    @OneToOne
    @JoinColumn(name="event_id", nullable = false)
    private Event event;

    @ManyToOne
    @JoinColumn(name="company_id", nullable = false)
    private Company company;
}
