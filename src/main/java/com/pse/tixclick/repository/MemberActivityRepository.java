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
                       WHEN COUNT(tixclick.dbo.member_activity.member_activity_id) > 0 THEN 1 
                       ELSE 0 
                   END AS HasConflict
                   FROM tixclick.dbo.member_activity
                   INNER JOIN tixclick.dbo.event_activity 
                       ON tixclick.dbo.member_activity.event_activity_id = tixclick.dbo.event_activity.event_activity_id
                   INNER JOIN tixclick.dbo.member 
                       ON tixclick.dbo.member_activity.member_id = tixclick.dbo.member.member_id
                   WHERE tixclick.dbo.member.account_id = :accountId
                     AND tixclick.dbo.event_activity.date_event = :dateEvent
                     AND (
                         -- Bắt đầu hoạt động của sự kiện trùng trong khoảng thời gian
                         (CAST(tixclick.dbo.event_activity.date_event AS DATETIME) + CAST(tixclick.dbo.event_activity.start_time_event AS DATETIME)) BETWEEN 
                             (CAST(:dateEvent AS DATETIME) + CAST(:startTimeEvent AS DATETIME)) 
                             AND (CAST(:dateEvent AS DATETIME) + CAST(:endTimeEvent AS DATETIME))
                         
                         OR
                         
                         -- Kết thúc hoạt động của sự kiện trùng trong khoảng thời gian
                         (CAST(tixclick.dbo.event_activity.date_event AS DATETIME) + CAST(tixclick.dbo.event_activity.end_time_event AS DATETIME)) BETWEEN 
                             (CAST(:dateEvent AS DATETIME) + CAST(:startTimeEvent AS DATETIME)) 
                             AND (CAST(:dateEvent AS DATETIME) + CAST(:endTimeEvent AS DATETIME))
                     )
               """, nativeQuery = true)
    int checkEventTimeConflict(@Param("accountId") Integer accountId,
                                   @Param("dateEvent") LocalDate dateEvent,
                                   @Param("startTimeEvent") LocalTime startTimeEvent,
                                   @Param("endTimeEvent") LocalTime endTimeEvent);

}
