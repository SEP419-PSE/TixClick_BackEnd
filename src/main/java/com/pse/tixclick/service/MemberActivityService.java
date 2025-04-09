package com.pse.tixclick.service;

import com.pse.tixclick.payload.dto.MemberActivityDTO;
import com.pse.tixclick.payload.request.create.CreateMemberActivityRequest;
import com.pse.tixclick.payload.response.BulkMemberActivityResult;
import com.pse.tixclick.payload.response.GetMemberActivityResponse;

import java.util.List;

public interface MemberActivityService {
    BulkMemberActivityResult addMemberActivities(CreateMemberActivityRequest request);

    List<GetMemberActivityResponse> getMemberActivitiesByEventActivityId(int eventActivityId);

    boolean deleteMemberActivity(int memberActivityId,int companyId);
}
