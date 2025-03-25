package com.pse.tixclick.controller;

import com.pse.tixclick.payment.CassoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/casso")
public class CassoController {
    @Autowired
    CassoService cassoService;

    /**
     * API để lấy danh sách giao dịch từ Casso.
     *
     * @param fromDate Ngày bắt đầu lấy giao dịch (định dạng: YYYY-MM-DD), có thể null.
     * @param page Số trang cần lấy, mặc định là 1.
     * @param pageSize Số lượng giao dịch trên mỗi trang, mặc định là 10.
     * @param sort Sắp xếp theo thời gian: "ASC" hoặc "DESC", mặc định là "ASC".
     * @return JSON chứa kết quả trả về từ API của Casso.
     */
    @GetMapping("/transactions")
    public String getTransactions(
            @RequestParam(required = false) String fromDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "ASC") String sort) {
        return cassoService.getTransactions(fromDate, page, pageSize, sort);
    }


}
