package com.emerchantpay.test.paymentsystembackend.repositories;

import com.emerchantpay.test.paymentsystembackend.model.Principal;
import com.google.common.util.concurrent.AtomicDouble;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MerchantRepository extends JpaRepository<Principal, Long> {
  Optional<Principal> findByEmail(String email);

  @Modifying
  @Query(
      value = "update principal set total_transaction_sum = ?2 where id = ?1",
      nativeQuery = true)
  void updateTotalTransactionSum(long merchantId, AtomicDouble totalSum);
}
