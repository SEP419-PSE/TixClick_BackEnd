package com.pse.tixclick.repository;

import com.pse.tixclick.payload.entity.entity_enum.EEventStatus;
import com.pse.tixclick.payload.entity.event.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Integer>, JpaSpecificationExecutor<Event> {
    Optional<Event> findEventByEventId(int eventId);

    Optional<Event> findEventByEventIdAndOrganizer_UserName(int eventId, String userName);

    List<Event> findEventsByStatus(EEventStatus status);

    Optional<List<Event>> findEventsByStatusAndOrganizer_UserName(String status, String userName);

    @Query("select e from Event e where e.organizer.accountId = :aId")
    Optional<List<Event>> findEventByOrganizerId(@Param("aId") int id);

    @Query("select e from Event e where e.organizer.accountId = :aId and e.status = :status")
    Optional<List<Event>> findEventByOrganizerIdAndStatus(@Param("aId") int id, @Param("status") EEventStatus status);

    @Query("SELECT COUNT(e) FROM Event e WHERE e.status = 'SCHEDULED'")
    int countTotalScheduledEvents();

    @Query("SELECT COALESCE(AVG(t.price), 0) FROM Ticket t")
    Double getAverageTicketPrice();

    @Query(value = """
            SELECT ec.category_name, 
                   (COUNT(e.event_id) * 100.0 / (SELECT COUNT(*) FROM events)) AS percentage
            FROM events e
            JOIN event_category ec ON e.category_id = ec.event_category_id
            GROUP BY ec.category_name
            """, nativeQuery = true)
    List<Object[]> getEventCategoryDistribution();

    @Query(value = "SELECT e FROM Event e WHERE e.status = 'SCHEDULED'")
    List<Event> findScheduledEvents();

    @Query(value = "SELECT e FROM Event e WHERE e.eventId = :id")
    Event findEvent(@Param("id") int id);

    @Query(value = "SELECT e.eventId FROM Event e WHERE e.status = 'SCHEDULED'")
    List<Integer> findScheduledEventIds();

    Optional<List<Event>> findEventsByCompany_CompanyId(int companyId);

    Optional<Event> findEventByEventIdAndCompany_RepresentativeId_UserName(int eventId, String userName);

    @Query("SELECT e FROM Event e " +
            "LEFT JOIN e.category c " +
            "LEFT JOIN e.tickets t " +
            "WHERE e.status = :status " +
            "AND (:eventName IS NULL OR LOWER(e.eventName) LIKE LOWER(CONCAT('%', :eventName, '%'))) " +
            "AND (:categoryId IS NULL OR c.eventCategoryId = :categoryId) " +
            "AND (:minPrice IS NULL OR t.price >= :minPrice) " +
            "AND (:city IS NULL OR LOWER(e.city) = LOWER(:city) OR (:city = 'other' AND e.city NOT IN :mainCities))")
    Page<Event> findEventsByFilters(
            @Param("status") EEventStatus status,
            @Param("eventName") String eventName,
            @Param("categoryId") Integer categoryId,
            @Param("minPrice") Double minPrice,
            @Param("city") String city,
            @Param("mainCities") List<String> mainCities,
            Pageable pageable);



    @Query("SELECT e FROM Event e WHERE e.eventCode = :eventCode")
    Optional<Event> findEventByEventCode(@Param("eventCode") String eventCode);

    List<Event> findEventsByCategory_EventCategoryIdAndStatus(int eventCategoryId,EEventStatus status);


    List<Event> findEventsByManagerId(int managerId);

    @Query("SELECT e FROM Event e WHERE " +
            "( :eventName IS NULL OR LOWER(e.eventName) LIKE %:eventName% ) " +
            "AND ( :eventCategoryId IS NULL OR e.category.eventCategoryId = :eventCategoryId ) " +
            "AND ( :minPrice IS NULL OR e.eventId IN (SELECT t.event.eventId FROM Ticket t WHERE t.price >= :minPrice) ) " +
            "AND ( :city IS NULL OR ( :city = 'other' AND e.city NOT IN ('Hồ Chí Minh', 'Hà Nội', 'Đà Nẵng') ) " +
            "OR LOWER(e.city) = LOWER(:city) )")
    List<Event> searchEvents(@Param("eventName") String eventName,
                             @Param("eventCategoryId") Integer eventCategoryId,
                             @Param("minPrice") Double minPrice,
                             @Param("city") String city);

}

