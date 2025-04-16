package com.pse.tixclick.service.impl;

import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.exception.ErrorCode;
import com.pse.tixclick.payload.dto.MemberActivityDTO;
import com.pse.tixclick.payload.entity.company.Member;
import com.pse.tixclick.payload.entity.company.MemberActivity;
import com.pse.tixclick.payload.entity.entity_enum.EStatus;
import com.pse.tixclick.payload.entity.entity_enum.ESubRole;
import com.pse.tixclick.payload.entity.event.Event;
import com.pse.tixclick.payload.request.create.CreateMemberActivityRequest;
import com.pse.tixclick.payload.response.BulkMemberActivityResult;
import com.pse.tixclick.payload.response.GetMemberActivityResponse;
import com.pse.tixclick.payload.response.GetMemberResponse;
import com.pse.tixclick.payload.response.MyEventResponse;
import com.pse.tixclick.repository.AccountRepository;
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

import java.util.*;
import java.util.stream.Collectors;

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
    AccountRepository accountRepository;

    @Override
    public BulkMemberActivityResult addMemberActivities(CreateMemberActivityRequest request) {
        var context = SecurityContextHolder.getContext();
        String userName = context.getAuthentication().getName();

        var eventActivity = eventActivityRepository.findById(request.getEventActivityId())
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_ACTIVITY_NOT_FOUND));

        var role = memberRepository.findMemberByAccount_UserNameAndCompany_CompanyId(
                userName,
                eventActivity.getEvent().getCompany().getCompanyId()
        ).orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND));

        if (role.getSubRole() != ESubRole.ADMIN && role.getSubRole() != ESubRole.OWNER) {
            throw new AppException(ErrorCode.NOT_PERMISSION);
        }

        List<MemberActivityDTO> success = new ArrayList<>();
        List<BulkMemberActivityResult.FailedMember> failed = new ArrayList<>();

        for (Integer memberId : request.getMemberIds()) {
            try {
                var member = memberRepository.findById(memberId)
                        .orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND));

                var optionalActivity = memberActivityRepository
                        .findMemberActivitiesByMemberAndEventActivity_EventActivityId(member, eventActivity.getEventActivityId());

                MemberActivity memberActivity;

                if (optionalActivity.isPresent()) {
                    memberActivity = optionalActivity.get();

                    if (memberActivity.getStatus() == EStatus.INACTIVE) {
                        memberActivity.setStatus(EStatus.ACTIVE);
                        memberActivityRepository.save(memberActivity);
                    } else {
                        failed.add(new BulkMemberActivityResult.FailedMember(memberId, "Already active"));
                        continue;
                    }

                } else {
                    memberActivity = new MemberActivity();
                    memberActivity.setMember(member);
                    memberActivity.setEventActivity(eventActivity);
                    memberActivity.setStatus(EStatus.ACTIVE);
                    memberActivityRepository.save(memberActivity);
                }

                MemberActivityDTO dto = modelMapper.map(memberActivity, MemberActivityDTO.class);
                success.add(dto);

            } catch (AppException ex) {
                failed.add(new BulkMemberActivityResult.FailedMember(memberId, ex.getMessage()));
            } catch (Exception e) {
                failed.add(new BulkMemberActivityResult.FailedMember(memberId, "Unexpected error"));
            }
        }

        return new BulkMemberActivityResult(success, failed);
    }



    @Override
    public List<GetMemberActivityResponse> getMemberActivitiesByEventActivityId(int eventActivityId) {
        var eventActivity = eventActivityRepository.findById(eventActivityId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_ACTIVITY_NOT_FOUND));

        List<MemberActivity> memberActivities = memberActivityRepository
                .findMemberActivitiesByEventActivity_EventActivityIdAndStatus(
                        eventActivityId, EStatus.ACTIVE);

        if (memberActivities.isEmpty()) {
            throw new AppException(ErrorCode.MEMBER_ACTIVITY_NOT_FOUND);
        }

        return memberActivities.stream().map(memberActivity -> {
            var member = memberActivity.getMember();
            var account = member.getAccount();

            GetMemberResponse memberResponse = GetMemberResponse.builder()
                    .memberId(member.getMemberId())
                    .userName(account.getUserName())
                    .email(account.getEmail())
                    .phoneNumber(account.getPhone())
                    .firstName(account.getFirstName())
                    .lastName(account.getLastName())
                    .subRole(String.valueOf(member.getSubRole()))
                    .status(String.valueOf(member.getStatus()))
                    .build();

            return GetMemberActivityResponse.builder()
                    .memberActivityId(memberActivity.getMemberActivityId())
                    .eventActivityId(memberActivity.getEventActivity().getEventActivityId())
                    .status(String.valueOf(memberActivity.getStatus()))
                    .member(memberResponse)
                    .build();
        }).collect(Collectors.toList());
    }



    @Override
    public boolean deleteMemberActivity(int memberActivityId, int companyId) {
        var context = SecurityContextHolder.getContext();
        String userName = context.getAuthentication().getName();

        // Tìm Member theo account và công ty
        Optional<Member> optionalMember = memberRepository.findMemberByAccount_UserNameAndCompany_CompanyId(
                userName,
                companyId
        );

        Member member = optionalMember.orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND));

        // Kiểm tra quyền hạn
        if (member.getSubRole() != ESubRole.ADMIN && member.getSubRole() != ESubRole.OWNER) {
            throw new AppException(ErrorCode.NOT_PERMISSION);
        }

        // Tìm activity
        var memberActivity = memberActivityRepository.findById(memberActivityId)
                .orElseThrow(() -> new AppException(ErrorCode.MEMBER_ACTIVITY_NOT_FOUND));

        if (memberActivity.getStatus() == EStatus.INACTIVE) {
            throw new AppException(ErrorCode.MEMBER_ACTIVITY_NOT_FOUND);
        }

        // Cập nhật trạng thái
        memberActivity.setStatus(EStatus.INACTIVE);
        memberActivityRepository.save(memberActivity);

        return true;
    }

    @Override
    public List<MyEventResponse> getMyEventActivities() {
        var context = SecurityContextHolder.getContext();
        String userName = context.getAuthentication().getName();

        List<Member> members = memberRepository.findMembersByAccount_UserName(userName);

        if (members.isEmpty()) {
            return Collections.emptyList();
        }

        // Map to hold events and their associated activities
        Map<Event, List<MyEventResponse.MyEventActivityResponse>> eventActivitiesMap = new HashMap<>();

        for (Member member : members) {
            List<MemberActivity> memberActivities = memberActivityRepository
                    .findMemberActivitiesByMember_MemberIdAndStatus(member.getMemberId(), EStatus.ACTIVE);

            if (memberActivities.isEmpty()) {
                continue;
            }

            for (MemberActivity memberActivity : memberActivities) {
                Event event = memberActivity.getEventActivity().getEvent();

                // Initialize the list of event activities if not already done
                eventActivitiesMap.putIfAbsent(event, new ArrayList<>());

                MyEventResponse.MyEventActivityResponse activityResponse = new MyEventResponse.MyEventActivityResponse(
                        memberActivity.getEventActivity().getActivityName(),
                        memberActivity.getEventActivity().getDateEvent(),
                        memberActivity.getEventActivity().getStartTimeEvent(),
                        memberActivity.getEventActivity().getEndTimeEvent()
                );

                // Add the activity to the map of event activities
                eventActivitiesMap.get(event).add(activityResponse);
            }
        }

        // Convert the map to the final response structure
        List<MyEventResponse> myEventActivities = eventActivitiesMap.entrySet().stream()
                .map(entry -> {
                    MyEventResponse response = new MyEventResponse();
                    response.setEventName(entry.getKey().getEventName()); // Event name
                    response.setEventActivities(entry.getValue()); // List of event activities
                    return response;
                })
                .collect(Collectors.toList());

        return myEventActivities;
    }


}
