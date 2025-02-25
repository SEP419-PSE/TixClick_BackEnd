package com.pse.tixclick.payload.entity.company;

import com.pse.tixclick.payload.entity.entity_enum.EStatus;
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

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    EStatus status;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    Member member;

    @ManyToOne
    @JoinColumn(name = "event_activity_id", nullable = false)
    EventActivity eventActivity;

}
