package com.pse.tixclick.service;

import com.pse.tixclick.payload.dto.MemberActivityDTO;
import com.pse.tixclick.payload.request.create.CreateMemberActivityRequest;

import java.util.List;

public interface MemberActivityService {
    List<MemberActivityDTO> addMemberActivities(CreateMemberActivityRequest request);
}
