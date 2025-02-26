package com.pse.tixclick.repository;

import com.pse.tixclick.payload.entity.payment.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoucherRepository extends JpaRepository<Voucher, Integer> {
}
