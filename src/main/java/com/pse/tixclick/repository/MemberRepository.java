package com.pse.tixclick.repository;

import com.pse.tixclick.payload.entity.company.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member,Integer> {
    Optional<Member> findMemberByAccount_AccountIdAndCompany_CompanyId(Integer accountId, Integer companyId);

    Optional<Member> findMemberByAccount_UserNameAndCompany_CompanyId(String userName, Integer companyId);
    Optional<Member> findMemberByAccount_UserNameAndCompany_CompanyId(String username, int companyId);

    Optional<List<Member>> findMembersByCompany_CompanyId(int companyId);

    Optional<Member> findMembersByAccount_Email(String email);

}
