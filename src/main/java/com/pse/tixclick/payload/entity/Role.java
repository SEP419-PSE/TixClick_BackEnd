package com.pse.tixclick.payload.entity;

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
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int userId;

    @Column
    String roleName;

    @OneToMany(mappedBy = "role")
    Collection<Account> accounts;
}
