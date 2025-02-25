package com.pse.tixclick.repository;

import com.pse.tixclick.payload.entity.company.CompanyAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyAccountRepository extends JpaRepository<CompanyAccount,Integer> {


}
