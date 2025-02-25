package com.pse.tixclick.repository;

import com.pse.tixclick.payload.entity.company.MemberActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberActivityRepository extends JpaRepository<MemberActivity,Integer> {
}
