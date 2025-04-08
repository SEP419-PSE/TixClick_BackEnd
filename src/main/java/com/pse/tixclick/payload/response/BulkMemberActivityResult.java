package com.pse.tixclick.payload.response;

import com.pse.tixclick.payload.dto.MemberActivityDTO;
import lombok.*;

import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class BulkMemberActivityResult {
    private List<MemberActivityDTO> success;
    private List<FailedMember> failed;

    @Getter
    @Setter
    @AllArgsConstructor
    public static class FailedMember {
        private Integer memberId;
        private String reason;
    }
}