package com.pse.tixclick.repository;

import com.pse.tixclick.payload.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {

    Optional<RefreshToken> findRefreshTokenByToken(String token);
    Optional<RefreshToken> findRefreshTokenByUserName(String userName);
}
