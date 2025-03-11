package com.pse.tixclick.repository;

import com.pse.tixclick.payload.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

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
    WITH ManagerWithLeastVerifications AS (
        SELECT TOP 1 a.account_id
        FROM Account a
        LEFT JOIN company_verification cv ON cv.submit_by_id = a.account_id
        WHERE a.role_id = 4
        GROUP BY a.account_id
        ORDER BY COUNT(cv.company_verification_id) ASC
    )
    SELECT * FROM Account a WHERE a.account_id = (SELECT account_id FROM ManagerWithLeastVerifications);
""", nativeQuery = true)
    Optional<Account> findManagerWithLeastVerifications();

    @Query(value = "SELECT COUNT(*) FROM Account a WHERE a.role.roleId = 1")
    int countTotalAdmins();

    @Query(value = "SELECT COUNT(*) FROM Account a WHERE a.role.roleId = 2")
    int countTotalBuyers();

    @Query(value = "SELECT COUNT(*) FROM Account a WHERE a.role.roleId = 3")
    int countTotalOrganizers();

    @Query(value = "SELECT COUNT(*) FROM Account a WHERE a.role.roleId = 4")
    int countTotalManagers();
}
