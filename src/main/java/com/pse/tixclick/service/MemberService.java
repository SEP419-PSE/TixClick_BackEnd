package com.pse.tixclick.service;

import com.pse.tixclick.payload.request.create.CreateMemberRequest;
import com.pse.tixclick.payload.response.MemberDTOResponse;

public interface MemberService {
    MemberDTOResponse createMember(CreateMemberRequest createMemberRequest);

    boolean deleteMember(int id);
 }
