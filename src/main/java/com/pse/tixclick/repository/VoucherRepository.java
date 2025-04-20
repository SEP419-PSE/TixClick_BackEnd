package com.pse.tixclick.repository;

import com.pse.tixclick.payload.entity.entity_enum.EVoucherStatus;
import com.pse.tixclick.payload.entity.payment.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Integer> {
    @Query("SELECT v FROM Voucher v WHERE v.voucherCode = :code AND v.event.eventId = :eventId")
    Optional<Voucher> findByVoucherCodeAndEvent(@Param("code") String code, @Param("eventId") int eventId);

    @Query("SELECT v FROM Voucher v WHERE v.voucherCode = :code")
    Voucher existsByVoucherCode(@Param("code") String code);

    @Query("SELECT v FROM Voucher v WHERE v.status = :status AND v.event.eventId = :eventId")
    List<Voucher> findByStatusAndEvent(@Param("status") String status, @Param("eventId") int eventId);

    List<Voucher> findVouchersByStatusAndEvent_EventId(EVoucherStatus status, int eventId);
}
