package com.pse.tixclick.payload.entity.team;

import com.pse.tixclick.payload.entity.Account;
import com.pse.tixclick.payload.entity.entity_enum.ESubRole;
import com.pse.tixclick.payload.entity.team.Team;
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

    @ManyToOne
    @JoinColumn(name = "team_id", nullable = false)
    Team team;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    Account account;

    @Column
    ESubRole subRole;
}

