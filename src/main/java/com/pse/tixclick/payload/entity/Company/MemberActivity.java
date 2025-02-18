package com.pse.tixclick.payload.entity.Company;

import com.pse.tixclick.payload.entity.event.EventActivity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class MemberActivity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int memberActivityId;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    Member member;

    @ManyToOne
    @JoinColumn(name = "event_activity_id", nullable = false)
    EventActivity eventActivity;

    @Column
    String status;
}
