package com.pse.tixclick.payload.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

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

    @Column
    String firstName;

    @Column
    String lastName;

    @Column(unique = true,nullable = false)
    String userName;

    @Column(nullable = false)
    String password;

    @Column(unique = true,nullable = false)
    String email;

    @Column
    String phone;

    @Column
    boolean active;

    @Column
    String dob;

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    Role role;
}
