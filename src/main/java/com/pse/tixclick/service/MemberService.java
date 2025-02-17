package com.pse.tixclick.service;

import com.pse.tixclick.payload.dto.MemberDTO;
import com.pse.tixclick.payload.request.CreateMemberRequest;
import com.pse.tixclick.payload.response.MemberDTOResponse;

public interface MemberService {
    MemberDTOResponse createMember(CreateMemberRequest createMemberRequest);
 }
