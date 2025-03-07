package com.pse.tixclick.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContractVerification extends JpaRepository<ContractVerification,Integer> {
}
