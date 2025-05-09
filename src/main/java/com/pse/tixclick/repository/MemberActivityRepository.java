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
        DECLARE @member_id INT = :memberId;
        DECLARE @event_activity_id INT = :eventActivityId;

        SELECT 
            CASE 
                WHEN ea.event_activity_id IS NULL THEN CAST(0 AS BIT)
                WHEN EXISTS (
                    SELECT 1
                    FROM [tixclick5].[dbo].[member_activity] ma
                    INNER JOIN [tixclick5].[dbo].[event_activity] ea2 ON ma.event_activity_id = ea2.event_activity_id
                    INNER JOIN [tixclick5].[dbo].[member] m ON ma.member_id = m.member_id
                    WHERE 
                        m.member_id = @member_id
                        AND ma.event_activity_id != @event_activity_id
                        AND ea2.date_event = ea.date_event
                        AND (
                            (ea2.start_time_event <= ea.end_time_event AND ea2.end_time_event >= ea.start_time_event)
                        )
                ) THEN CAST(1 AS BIT)
                ELSE CAST(0 AS BIT)
            END AS is_conflict
        FROM [tixclick5].[dbo].[event_activity] ea
        LEFT JOIN [tixclick5].[dbo].[member_activity] ma ON ea.event_activity_id = ma.event_activity_id 
            AND ma.member_id = @member_id
        LEFT JOIN [tixclick5].[dbo].[member] m ON ma.member_id = m.member_id
        WHERE ea.event_activity_id = @event_activity_id
        """, nativeQuery = true)
    boolean checkEventConflict(@Param("memberId") int memberId, @Param("eventActivityId") int eventActivityId);
}
