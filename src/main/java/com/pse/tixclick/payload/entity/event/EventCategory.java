package com.pse.tixclick.payload.entity.event;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Collection;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class EventCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int eventCategoryId;

    @Column(columnDefinition = "NVARCHAR(255)")
    String categoryName;

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    Collection<Event> events;
}
