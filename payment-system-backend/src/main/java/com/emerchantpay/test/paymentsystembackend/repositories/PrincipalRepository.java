package com.emerchantpay.test.paymentsystembackend.repositories;

import com.emerchantpay.test.paymentsystembackend.model.Principal;
import com.emerchantpay.test.paymentsystembackend.model.PrincipalType;
import jakarta.persistence.LockModeType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface PrincipalRepository extends JpaRepository<Principal, Long> {
  @EntityGraph(value = "Principal.transactionList")
  Optional<Principal> findByEmail(String email);

  @EntityGraph(value = "Principal.transactionList")
  Optional<Principal> findById(long id);

  // don't load transactionList
  Optional<Principal> findPrincipalById(long id);

  @EntityGraph(value = "Principal.transactionList")
  List<Principal> findAllByPrincipalType(PrincipalType principalType);

  @Query(
      value =
          "select p.id, p.name,p.password, p.description,p.email,p.principal_type, p.status, p.total_transaction_sum, p.version"
              + " from principal p where p.id = ?1 and p.principal_type = ?2 ", nativeQuery = true)
  Optional<Principal> findByIdAndPrincipalType( long id, int type);

  @Modifying
  @Query(
          value = "update Principal p set p.totalTransactionSum =:totalSum where p.id =:merchantId")
  void updateTotalTransactionSum(@Param("merchantId") long merchantId, @Param("totalSum")  BigDecimal totalSum);

}
