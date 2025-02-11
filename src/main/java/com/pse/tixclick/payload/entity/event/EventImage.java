package com.pse.tixclick.payload.entity.event;

import com.pse.tixclick.payload.entity.entity_enum.EEventImageType;
import com.pse.tixclick.payload.entity.event.Event;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class EventImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int eventImageId;

    @Column
    String imagePath;

    @Column
    String imageCode;

    @Column
    EEventImageType imageType;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    Event event;

    @Column
    boolean status;
}
