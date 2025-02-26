package com.pse.tixclick.repository;

import com.pse.tixclick.payload.entity.payment.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface VoucherRepository extends JpaRepository<Voucher, Integer> {
    @Query("SELECT v FROM Voucher v WHERE v.voucherCode = :code")
    Optional<Voucher> findByVoucherCode(@Param("code") String code);
}
