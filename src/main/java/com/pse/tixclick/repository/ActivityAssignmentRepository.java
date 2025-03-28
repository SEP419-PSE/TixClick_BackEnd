package com.pse.tixclick.repository;

import com.pse.tixclick.payload.entity.seatmap.ActivityAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ActivityAssignmentRepository extends JpaRepository<ActivityAssignment, Integer> {
    @Query(value = "SELECT aa FROM ActivityAssignment aa WHERE aa.ticketPurchase.ticketPurchaseId = :ticketPurchaseId AND aa.status = 'RESERVED'")
    Optional<ActivityAssignment> findActivityAssignmentByTicketPurchaseAndReserved(@Param("ticketPurchaseId") int ticketPurchaseId);

    @Query(value = "SELECT aa FROM ActivityAssignment aa WHERE aa.ticketPurchase.ticketPurchaseId = :ticketPurchaseId AND aa.status = 'SOLD'")
    Optional<ActivityAssignment> findActivityAssignmentByTicketPurchaseAndSold(@Param("ticketPurchaseId") int ticketPurchaseId);



}
