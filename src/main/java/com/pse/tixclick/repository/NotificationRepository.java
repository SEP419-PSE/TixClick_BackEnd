package com.pse.tixclick.repository;

import com.pse.tixclick.payload.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification,Integer> {
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.account.userName = :userName")
    int countNotificationByAccountId(@Param("userName") String userName);

    Optional<Notification> findTopByAccount_UserNameOrderByCreatedDateAsc(String userName);

    List<Notification> findNotificationsByAccount_UserName(String username);
}
