package com.pse.tixclick.repository;

import com.pse.tixclick.payload.entity.event.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event,Integer> {
    Optional<Event> findEventByEventId(int eventId);

    Optional<Event> findEventByEventIdAndOrganizer_UserName(int eventId, String userName);

    Optional<List<Event>> findEventsByStatus(String status);

    Optional<List<Event>> findEventsByStatusAndOrganizer_UserName(String status, String userName);

    @Query("select e from Event e where e.organizer.accountId = :aId")
    Optional<List<Event>> findEventByOrganizerId(@Param("aId") int id);

    @Query("select e from Event e where e.organizer.accountId = :aId and e.status = :status")
    Optional<List<Event>> findEventByOrganizerIdAndStatus(@Param("aId") int id, @Param("status") String status);

    Optional<List<Event>> findEventsByCompany_CompanyId(int companyId);

    Optional<Event> findEventByEventIdAndCompany_RepresentativeId_UserName(int eventId, String userName);
}
