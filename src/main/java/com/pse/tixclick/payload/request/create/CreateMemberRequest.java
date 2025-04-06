package com.pse.tixclick.payload.request.create;

import com.pse.tixclick.payload.dto.MailListDTO;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateMemberRequest {
    int companyId;
    List<MailListDTO> mailList;
}
