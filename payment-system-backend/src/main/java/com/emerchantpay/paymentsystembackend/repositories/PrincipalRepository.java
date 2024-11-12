package com.emerchantpay.paymentsystembackend.repositories;

import com.emerchantpay.paymentsystembackend.model.Principal;
import com.emerchantpay.paymentsystembackend.model.PrincipalType;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PrincipalRepository extends JpaRepository<Principal, Long> {
  @Query("SELECT new com.emerchantpay.paymentsystembackend.model.Principal(p.id, p.name, p.description, p.email, " +
          "p.principalType, p.principalStatus, p.totalTransactionSum, p.version) " +
          "FROM Principal p WHERE p.email = ?1")
  Optional<Principal> findByEmail(String email);

  @Query(value = "select p.id, p.email, p.total_transaction_sum," +
          "(select count(t.uuid) from transactions t" +
          " where t.principal_id = :id)" +
          " as count_transactions, " +
          "t.uuid, t.amount, t.customer_email,t.customer_phone,t.reference_transaction_uuid, " +
          "t.transaction_type, t.transaction_status,t.timestamp " +
          "from principal p " +
          "LEFT JOIN transactions t ON p.id = t.principal_id " +
          " WHERE p.id = :id order by t.timestamp desc" +
          " LIMIT :limit OFFSET :offset", nativeQuery = true
         )
  List<Tuple> findPrincipalByIdWithBatchOfTransactions(@Param("id") long id, @Param("offset") int offset, @Param("limit") int limit);

  // don't load transactionList
  @Query("SELECT new com.emerchantpay.paymentsystembackend.model.Principal(p.id, p.name, p.description, p.email, " +
          "p.principalType, p.principalStatus, p.totalTransactionSum, p.version) " +
          "FROM Principal p WHERE p.id = ?1")
  Optional<Principal> findPrincipalById(long id);

  @Query("SELECT new com.emerchantpay.paymentsystembackend.model.Principal(p.id, p.name, p.description, p.email, " +
          "p.principalType, p.principalStatus, p.totalTransactionSum, p.version) " +
          "FROM Principal p WHERE p.principalType = ?1")
  List<Principal> findAllByPrincipalType(PrincipalType principalType);

  @Query(
      value =
          "SELECT new com.emerchantpay.paymentsystembackend.model.Principal(p.id, p.name, p.description, p.email, "
              + " p.principalType, p.principalStatus, p.totalTransactionSum,p.version) " +
                  "from Principal p WHERE p.id = ?1 AND p.principalType = ?2 ")
  Optional<Principal> findByIdAndPrincipalType(long id, PrincipalType type);
}
