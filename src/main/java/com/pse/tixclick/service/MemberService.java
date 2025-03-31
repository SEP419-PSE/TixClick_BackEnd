package com.pse.tixclick.service;

import com.pse.tixclick.payload.dto.MemberDTO;
import com.pse.tixclick.payload.request.create.CreateMemberRequest;
import com.pse.tixclick.payload.response.MemberDTOResponse;
import com.pse.tixclick.payload.response.SearchAccountResponse;

import java.util.List;

public interface MemberService {
    MemberDTOResponse createMember(CreateMemberRequest createMemberRequest);

    boolean deleteMember(int id);

    List<MemberDTO>  getMembersByCompanyId(int companyId);


}
