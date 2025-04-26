package com.pse.tixclick.payload.entity;

import com.pse.tixclick.payload.entity.entity_enum.ERole;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.*;

import java.util.Collection;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Builder
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int roleId;

    @Enumerated(EnumType.STRING)
    @Column
    ERole roleName;

    @OneToMany(mappedBy = "role", fetch = FetchType.LAZY)
    Collection<Account> accounts;
}
