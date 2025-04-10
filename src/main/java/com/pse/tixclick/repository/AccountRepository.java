package com.pse.tixclick.repository;

import com.pse.tixclick.payload.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {
    Optional<Account> findAccountByUserName(String username);

    Optional<Account> findAccountByEmail(String email);

    boolean existsAccountByUserName(String username);
    boolean existsAccountByEmail(String email);

//    @Query(value = """
//    WITH ManagerWithLeastVerifications AS (
//        SELECT TOP 1 a.account_id
//        FROM Account a
//        LEFT JOIN company_verification cv ON cv.submit_by_id = a.account_id
//        WHERE a.role_id = 4
//        GROUP BY a.account_id
//        ORDER BY COUNT(cv.company_verification_id) ASC
//    )
//    SELECT * FROM Account a WHERE a.account_id = (SELECT account_id FROM ManagerWithLeastVerifications);
//""", nativeQuery = true)

    @Query(value = """
    WITH ManagerVerifications AS (
        -- Đếm số lượng xác minh của từng Manager trong cả hai loại verification
        SELECT\s
            a.account_id,\s
            COALESCE(COUNT(cv.company_verification_id), 0)\s
            + COALESCE(COUNT(cvt.contract_verification_id), 0) AS verification_count
        FROM Account a
        LEFT JOIN company_verification cv ON cv.submit_by_id = a.account_id
        LEFT JOIN contract_verification cvt ON cvt.verified_by_id = a.account_id
        WHERE a.role_id = 4
        GROUP BY a.account_id
    ),
    
    PendingManagers AS (
        -- Lấy danh sách Manager đang có PENDING trong bất kỳ loại verification nào
        SELECT DISTINCT submit_by_id AS account_id FROM company_verification WHERE status = 'PENDING'
        UNION
        SELECT DISTINCT verified_by_id AS account_id FROM contract_verification WHERE status = 'PENDING'
    ),
    
    CheckNoPending AS (
        -- Kiểm tra xem có manager nào không có PENDING không
        SELECT COUNT(*) AS count_no_pending
        FROM ManagerVerifications mv
        LEFT JOIN PendingManagers pm ON mv.account_id = pm.account_id
        WHERE pm.account_id IS NULL
    ),
    
    AvailableManagers AS (
        -- Nếu có manager chưa có PENDING, chọn từ nhóm này
        SELECT mv.account_id, mv.verification_count
        FROM ManagerVerifications mv
        LEFT JOIN PendingManagers pm ON mv.account_id = pm.account_id
        WHERE pm.account_id IS NULL
    
        UNION ALL
    
        -- Nếu tất cả đã có PENDING, chọn người có ít xác minh nhất
        SELECT TOP 1 mv.account_id, mv.verification_count
        FROM ManagerVerifications mv
        WHERE (SELECT count_no_pending FROM CheckNoPending) = 0
        ORDER BY mv.verification_count ASC
    )
    
    -- Lấy manager phù hợp nhất một cách ngẫu nhiên (nếu có nhiều)
    SELECT TOP 1 a.*\s
    FROM Account a
    JOIN AvailableManagers am ON a.account_id = am.account_id
    ORDER BY NEWID();
    
""", nativeQuery = true)
    Optional<Account> findManagerWithLeastVerifications();


    @Query(value = "SELECT * FROM Account a WHERE a.role_id IN (1, 4)", nativeQuery = true)
    List<Account> findAccountsByRoleManagerAndAdmin();


    @Query(value = "SELECT COUNT(*) FROM Account a WHERE a.role.roleId = 1")
    Integer countTotalAdmins();

    @Query(value = "SELECT COUNT(*) FROM Account a WHERE a.role.roleId = 2")
    Integer countTotalBuyers();

    @Query(value = "SELECT COUNT(*) FROM Account a WHERE a.role.roleId = 3")
    int countTotalOrganizers();

    @Query(value = "SELECT COUNT(*) FROM Account a WHERE a.role.roleId = 4")
    int countTotalManagers();

    @Query(value = "SELECT COUNT(*) FROM Account")
    int countTotalAccounts();

    List<Account> findAccountsByRole_RoleId(int roleId);

    @Query(value = "SELECT * " +
            "FROM account " +
            "WHERE email LIKE CONCAT('%', :email, '%@gmail.com')", nativeQuery = true)
    List<Account> searchAccountByEmail(@Param("email") String email);

}
