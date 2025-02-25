package com.pse.tixclick.service.impl;

import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.exception.ErrorCode;
import com.pse.tixclick.payload.dto.MemberActivityDTO;
import com.pse.tixclick.payload.entity.company.MemberActivity;
import com.pse.tixclick.payload.entity.entity_enum.EStatus;
import com.pse.tixclick.payload.entity.entity_enum.ESubRole;
import com.pse.tixclick.payload.request.create.CreateMemberActivityRequest;
import com.pse.tixclick.repository.EventActivityRepository;
import com.pse.tixclick.repository.MemberActivityRepository;
import com.pse.tixclick.repository.MemberRepository;
import com.pse.tixclick.service.MemberActivityService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MemberActivityServiceImpl implements MemberActivityService {
    MemberActivityRepository memberActivityRepository;
    MemberRepository memberRepository;
    EventActivityRepository eventActivityRepository;
    ModelMapper modelMapper;
    @Override
    public List<MemberActivityDTO> addMemberActivities(CreateMemberActivityRequest request) {
        var context  = SecurityContextHolder.getContext();
        String userName = context.getAuthentication().getName();

        var eventActivity = eventActivityRepository.findById(request.getEventActivityId())
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_ACTIVITY_NOT_FOUND));

        var role = memberRepository.findMemberByAccount_UserNameAndCompany_CompanyId(userName, eventActivity.getEvent().getCompany().getCompanyId())
                .orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND));

        if(role.getSubRole() != ESubRole.ADMIN && role.getSubRole() != ESubRole.OWNER){
            throw new AppException(ErrorCode.NOT_PERMISSION);
        }

        List<MemberActivityDTO> dtos = new ArrayList<>();

        for (Integer memberId : request.getMemberIds()) {
            var member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND));

            var memberActivity = new MemberActivity();
            memberActivity.setStatus(EStatus.ACTIVE);
            memberActivity.setMember(member);
            memberActivity.setEventActivity(eventActivity);

            // Lưu vào DB
            memberActivityRepository.save(memberActivity);

            // Sử dụng ModelMapper để chuyển đổi Entity sang DTO
            MemberActivityDTO dto = modelMapper.map(memberActivity, MemberActivityDTO.class);
            dtos.add(dto);
        }

        return dtos;


    }
}
