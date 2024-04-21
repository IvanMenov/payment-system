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
          "select t.uuid,t.amount, t.status, t.customer_email, t.customer_phone, t.reference_transaction_uuid"
              + " from transactions t where merchant_id = ?1 order by timestamp desc",
      nativeQuery = true)
  List<Transaction> findAllByMerchantId(Long id);

  @Query(
      value =
          "select t.uuid,t.amount, t.status, t.customer_email, t.customer_phone, t.reference_transaction_uuid"
              + " from transactions t group by merchant_id order by timestamp desc",
      nativeQuery = true)
  List<Transaction> findAllTransactionsGroupByMerchant();
}
