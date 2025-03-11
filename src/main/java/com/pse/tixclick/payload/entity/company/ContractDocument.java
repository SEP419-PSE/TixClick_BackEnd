package com.pse.tixclick.payload.entity.company;

import com.pse.tixclick.payload.entity.Account;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class ContractDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int contractDocumentId;

    @ManyToOne
    @JoinColumn(name="contract_id", nullable = false)
    Contract contract;

    @Column
    String fileName;

    @Column
    String fileURL;

    @Column
    String fileType;

    @Column
    String status;

    @ManyToOne
    @JoinColumn(name="uploaded_by", nullable = false)
    Account uploadedBy;

    @Column
    LocalDateTime uploadDate;
}
