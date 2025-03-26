package com.pse.tixclick.payload.entity.company;

import com.pse.tixclick.payload.entity.Account;
import com.pse.tixclick.payload.entity.event.Event;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Collection;
import java.util.List;

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

    @Column
    private String contractName;

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

    @OneToMany(mappedBy = "contract",fetch = FetchType.LAZY)
    private Collection<ContractDetail> contractDetails;
}
