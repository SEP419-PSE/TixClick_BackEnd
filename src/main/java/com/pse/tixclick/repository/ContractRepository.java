package com.pse.tixclick.repository;

import com.pse.tixclick.payload.entity.company.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContractRepository extends JpaRepository<Contract,Integer> {
    @Query(value = "SELECT c FROM Contract c WHERE c.event.eventId = :eventId")
    Contract findByEventId(@Param("eventId") int eventId);


}
