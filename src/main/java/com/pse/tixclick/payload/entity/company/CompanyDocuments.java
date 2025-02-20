package com.pse.tixclick.payload.entity.company;

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
public class CompanyDocuments {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int contractId;

    @ManyToOne
    @JoinColumn(name="company_id", nullable = false)
    private Company company;

    @Column
    private String fileName;

    @Column
    private String fileURL;

    @Column
    private String fileType;

    @Column
    private LocalDateTime uploadDate;
}
