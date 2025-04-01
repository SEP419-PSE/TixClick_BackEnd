package com.pse.tixclick.payload.entity.company;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pse.tixclick.payload.entity.Account;
import com.pse.tixclick.payload.entity.entity_enum.EContractStatus;
import com.pse.tixclick.payload.entity.event.Event;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
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

    @Column(columnDefinition = "NVARCHAR(255)")
    private String contractName;


    @Column(nullable = false)
    private double totalAmount;

    @Column(nullable = false)
    private String commission;

    @Column(nullable = false)
    private String contractType;

    @Column
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDate startDate;

    @Column
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDate endDate;

    @Column
    @Enumerated(EnumType.STRING)
    private EContractStatus status;


    @ManyToOne
    @JoinColumn(name="manager_id", nullable = false)
    private Account account;

    @OneToOne
    @JoinColumn(name="event_id", nullable = false)
    private Event event;

    @ManyToOne
    @JoinColumn(name="company_id", nullable = false)
    private Company company;

    @OneToMany(mappedBy = "contract", fetch = FetchType.LAZY)
    private Collection<ContractDetail> contractDetails;

    @OneToMany(mappedBy = "contract", fetch = FetchType.LAZY)
    private Collection<ContractDocument> contractDocuments;
}
