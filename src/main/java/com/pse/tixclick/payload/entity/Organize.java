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
public class Organize {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int groupId;

    @Column
    String groupName;


}
