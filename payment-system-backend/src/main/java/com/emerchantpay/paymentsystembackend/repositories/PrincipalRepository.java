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
  @EntityGraph(value = "Principal.transactionList")
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
  Optional<Principal> findPrincipalById(long id);

  @EntityGraph(value = "Principal.transactionList")
  List<Principal> findAllByPrincipalType(PrincipalType principalType);

  @Query(
      value =
          "select p.id, p.name, p.description,p.email,p.principal_type, p.principal_status, p.total_transaction_sum, p.version"
              + " from principal p where p.id = ?1 and p.principal_type = ?2 ",
      nativeQuery = true)
  Optional<Principal> findByIdAndPrincipalType(long id, String type);
}
