package com.pse.tixclick.payload.response;

import com.pse.tixclick.payload.dto.MemberDTO;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MemberDTOResponse {
    private List<MemberDTO> createdMembers;
    private List<String> sentEmails;

}
