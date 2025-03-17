package com.pse.tixclick.config.testnotification;

import java.security.Principal;

// Custom Principal để Spring Boot nhận diện user
public class UserPrincipal implements Principal {
    private final String name;

    public UserPrincipal(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
