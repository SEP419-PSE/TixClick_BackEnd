package com.pse.tixclick.payload.entity.Company;

import com.pse.tixclick.payload.entity.Account;
import com.pse.tixclick.payload.entity.entity_enum.ESubRole;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int memberId;

    @Column
    ESubRole subRole;

    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    Company company;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    Account account;


}

