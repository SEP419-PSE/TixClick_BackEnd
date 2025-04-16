package com.pse.tixclick.repository;

import com.pse.tixclick.payload.entity.company.Member;
import com.pse.tixclick.payload.entity.company.MemberActivity;
import com.pse.tixclick.payload.entity.entity_enum.EStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberActivityRepository extends JpaRepository<MemberActivity,Integer> {
    List<MemberActivity> findMemberActivitiesByEventActivity_EventActivityIdAndStatus(int id, EStatus status);

    Optional<MemberActivity> findMemberActivitiesByMemberAndEventActivity_EventActivityId(Member member, int eventActivityId);

    List<MemberActivity> findMemberActivitiesByMember_MemberIdAndStatus(int memberId, EStatus status);
}
