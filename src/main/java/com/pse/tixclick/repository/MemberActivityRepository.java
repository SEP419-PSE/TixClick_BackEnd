package com.pse.tixclick.repository;

import com.pse.tixclick.payload.entity.company.Member;
import com.pse.tixclick.payload.entity.company.MemberActivity;
import com.pse.tixclick.payload.entity.entity_enum.EStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MemberActivityRepository extends JpaRepository<MemberActivity,Integer> {
    List<MemberActivity> findMemberActivitiesByEventActivity_EventActivityIdAndStatus(int id, EStatus status);

    Optional<MemberActivity> findMemberActivitiesByMemberAndEventActivity_EventActivityId(Member member, int eventActivityId);

    List<MemberActivity> findMemberActivitiesByMember_MemberIdAndStatus(int memberId, EStatus status);

    @Query(value = """
        SELECT CASE 
            WHEN COUNT(ma.member_activity_id) > 0 THEN 1 
            ELSE 0 
        END AS HasConflict
        FROM tixclick.dbo.member_activity ma
        INNER JOIN tixclick.dbo.event_activity ea
            ON ma.event_activity_id = ea.event_activity_id
        INNER JOIN tixclick.dbo.member m
            ON ma.member_id = m.member_id
        WHERE m.account_id = :accountId
          AND ea.date_event = :dateEvent
          AND (
            -- Check start time trong khoảng
            CAST(CONVERT(VARCHAR(10), ea.date_event, 120) + ' ' + CONVERT(VARCHAR(8), ea.start_time_event, 108) AS DATETIME) BETWEEN 
                CAST(CONCAT(:dateEvent, ' ', :startTimeEvent) AS DATETIME) 
                AND CAST(CONCAT(:dateEvent, ' ', :endTimeEvent) AS DATETIME)

            OR

            -- Check end time trong khoảng
            CAST(CONVERT(VARCHAR(10), ea.date_event, 120) + ' ' + CONVERT(VARCHAR(8), ea.end_time_event, 108) AS DATETIME) BETWEEN 
                CAST(CONCAT(:dateEvent, ' ', :startTimeEvent) AS DATETIME) 
                AND CAST(CONCAT(:dateEvent, ' ', :endTimeEvent) AS DATETIME)
          )
    """, nativeQuery = true)
    int checkEventTimeConflict(
            @Param("accountId") Integer accountId,
            @Param("dateEvent") LocalDate dateEvent,
            @Param("startTimeEvent") LocalTime startTimeEvent,   // Dùng String "HH:mm:ss"
            @Param("endTimeEvent") LocalTime endTimeEvent
    );
}
