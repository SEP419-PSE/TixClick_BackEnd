package com.pse.tixclick.repository;

import com.pse.tixclick.payload.entity.Company.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member,Integer> {
}
