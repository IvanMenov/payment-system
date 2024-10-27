package com.emerchantpay.paymentsystembackend.repositories;

import com.emerchantpay.paymentsystembackend.model.Transaction;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {
  @Query(
      value =
          "select t.uuid,t.amount, t.transaction_status, t.transaction_type, t.customer_email," +
                  " t.customer_phone, t.reference_transaction_uuid,"
              + " t.principal_id, t.timestamp"
              + " from transactions t where principal_id = ?1 order by timestamp desc limit ?2 offset ?3",
      nativeQuery = true)
  List<Transaction> findBatchOfTransactionsByMerchantId(Long id, int limit, int offset);

  @Modifying
  @Query(value = "delete from transactions t where t.uuid in (:uuids)", nativeQuery = true)
  void deleteTransactionsOlderThan(@Param("uuids") List<String> transactionUuids);

  @Query(
      value =
          "select t.uuid,t.amount, t.transaction_status, t.transaction_type, t.customer_email, t.customer_phone, t.reference_transaction_uuid,"
              + "t.principal_id, t.timestamp "
              + "from transactions t where t.timestamp < ?1",
      nativeQuery = true)
  List<Transaction> getTransactionsOlderThanAnHour(long timestamp);

  @Query(
      value = "select count(t.uuid) from transactions t where t.principal_id = ?1",
      nativeQuery = true)
  int hasTransactionForMerchant(long id);

  @Query(value = "delete from transactions t where t.principal_id = ?1", nativeQuery = true)
  @Modifying
  void deleteTransactionsForMerchant(long merchantId);
}
