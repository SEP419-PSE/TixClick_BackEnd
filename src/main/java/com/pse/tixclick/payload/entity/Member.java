package com.pse.tixclick.payload.entity;

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
    int memberId; // ID của Member, không nên trùng tên với khóa ngoại

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    Organize organize;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false) // Sửa từ member_id thành account_id
    Account account;

    @Column
    ESubRole subRole;
}

