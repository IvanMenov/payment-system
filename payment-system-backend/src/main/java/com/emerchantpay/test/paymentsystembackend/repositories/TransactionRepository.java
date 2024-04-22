package com.emerchantpay.test.paymentsystembackend.repositories;

import com.emerchantpay.test.paymentsystembackend.model.Transaction;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {
  @Query(
      value =
          "select t.uuid,t.amount, t.status, t.type, t.customer_email, t.customer_phone, t.reference_transaction_uuid,"
              + " t.principal_id, t.timestamp"
              + " from transactions t where principal_id = ?1 order by timestamp desc",
      nativeQuery = true)
  List<Transaction> findAllByMerchantId(Long id);

  @Query(
      value =
          "select t.uuid,t.amount, t.status,t.type, t.customer_email, t.customer_phone, t.reference_transaction_uuid,"
              + " t.principal_id, t.timestamp"
              + " from transactions t order by t.principal_id ,t.timestamp desc",
      nativeQuery = true)
  List<Transaction> findAllTransactionsGroupByMerchant();

  @Query(value = "delete from transactions t where t.timestamp < ?1", nativeQuery = true)
  void deleteTransactionsOlderThan(long timestamp);
}
