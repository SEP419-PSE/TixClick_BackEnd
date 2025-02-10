package com.pse.tixclick.config;

import com.pse.tixclick.payload.entity.Account;
import com.pse.tixclick.repository.AccountRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class Util {
    @Autowired
    private final AccountRepository accountRepository;

    public Account getAccountFromAuthentication(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return accountRepository.findAccountByUserName(authentication.getName())
                .orElseThrow(() -> new EntityNotFoundException(new Exception("UserName not found!!!")));
    }
}
