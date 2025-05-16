package com.pse.tixclick.repository;

import com.pse.tixclick.payload.entity.CheckinLog;
import com.pse.tixclick.payload.entity.entity_enum.ECheckinLogStatus;
import com.pse.tixclick.payload.response.TicketCheckinStatsProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CheckinLogRepository extends JpaRepository<CheckinLog, Integer> {
    @Query(value = "SELECT COUNT(*) FROM CheckinLog WHERE checkinStatus = 'CHECKED_IN'")
    int countTotalCheckins();



    @Query(value = """
        SELECT 
            t.ticket_name AS ticketName,
            t.ticket_id AS ticketId,
            COUNT(tp.ticket_purchase_id) AS totalPurchased,
            COUNT(CASE WHEN cl.checkin_status = 'CHECKED_IN' THEN 1 END) AS checkedIn,
            COUNT(CASE WHEN cl.checkin_status IS NULL OR cl.checkin_status = 'PENDING' THEN 1 END) AS notCheckedIn
        FROM 
            ticket t
        LEFT JOIN 
            ticket_purchase tp ON t.ticket_id = tp.ticket_id
        LEFT JOIN 
            checkin_log cl ON tp.ticket_purchase_id = cl.ticket_purchase_id
        WHERE 
            tp.event_activity_id = :eventActivityId
        GROUP BY 
            t.ticket_name, t.ticket_id
        """, nativeQuery = true)
    List<TicketCheckinStatsProjection> getTicketCheckinStatsByEventActivityId(@Param("eventActivityId") int eventActivityId);

    @Query(value = """
      SELECT COUNT(cl.checkin_id) AS total_checkins
            FROM checkin_log cl
            JOIN orders o ON cl.order_id = o.order_id
            JOIN order_detail od ON o.order_id = od.order_id
            JOIN ticket_purchase tp ON od.ticket_purchase_id = tp.ticket_purchase_id
            JOIN event_activity ea ON tp.event_activity_id = ea.event_activity_id
            WHERE ea.event_activity_id = :eventActivityId AND cl.checkin_status = :checkinStatus
    """,nativeQuery = true)
    int countTotalCheckinsByEventActivityIdAndCheckinStatus(@Param("eventActivityId") int eventActivityId,
                                            @Param("checkinStatus") String checkinStatus);

    @Query(value = """
      SELECT COUNT(cl.checkin_id) AS total_checkins
            FROM checkin_log cl
            JOIN orders o ON cl.order_id = o.order_id
            JOIN order_detail od ON o.order_id = od.order_id
            JOIN ticket_purchase tp ON od.ticket_purchase_id = tp.ticket_purchase_id
            JOIN event_activity ea ON tp.event_activity_id = ea.event_activity_id
            WHERE ea.event_activity_id = :eventActivityId 
    """,nativeQuery = true)
    int countTotalCheckinsByEventActivityId(@Param("eventActivityId") int eventActivityId);
}
