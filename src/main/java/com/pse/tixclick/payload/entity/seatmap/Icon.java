package com.pse.tixclick.payload.entity.seatmap;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class Icon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int iconId;

    @Column(columnDefinition = "NVARCHAR(255)")
    String iconName;

    @Column
    String xPosition;

    @Column
    String yPosition;

    @Column
    String url;

    @Column
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdDate;

    @Column
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime updatedDate;

    @ManyToOne
    @JoinColumn(name = "seatmap_id", nullable = false)
    SeatMap seatMap;

    @Column
    boolean status;


}
