package com.pse.tixclick.mapper;
import com.pse.tixclick.payload.dto.AccountDTO;
import com.pse.tixclick.payload.entity.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface AccountMapper {
    @Mapping(source = "role.roleId", target = "roleId")  // Ánh xạ ID của Role sang roleId
    AccountDTO toDTO(Account account);
    Account toEntity(AccountDTO accountDTO);
}
