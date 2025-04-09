package com.pse.tixclick.repository;

import com.pse.tixclick.payload.entity.payment.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Integer> {
    @Query("SELECT v FROM Voucher v WHERE v.voucherCode = :code")
    Optional<Voucher> findByVoucherCode(@Param("code") String code);

    @Query("SELECT CASE WHEN COUNT(v) > 0 THEN true ELSE false END FROM Voucher v WHERE v.voucherCode = :code")
    boolean existsByVoucherCode(@Param("code") String code);
}
