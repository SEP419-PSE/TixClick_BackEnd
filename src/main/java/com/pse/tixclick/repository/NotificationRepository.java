package com.pse.tixclick.repository;

import com.pse.tixclick.payload.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification,Integer> {
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.account.accountId = :accountId")
    int countNotificationByAccountId(@Param("accountId") int accountId);

    Optional<Notification> findTopByAccount_AccountIdOrderByCreatedDateAsc(int accountId);
}
