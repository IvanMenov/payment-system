package com.emerchantpay.test.paymentsystembackend.repositories;

import com.emerchantpay.test.paymentsystembackend.model.Principal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PrincipalRepository extends JpaRepository<Principal, Long> {
  // @Query(value = "select p from Principal p join fetch p.transactionList tl WHERE p.email = ?1")
  Optional<Principal> findByEmail(String email);

  @Query(
      value =
          "select p.id, p.name,p.password, p.description,p.email,p.principal_type, p.status, p.total_transaction_sum"
              + " from principal p where p.principal_type = ?1",
      nativeQuery = true)
  List<Principal> findAllByPrincipalType(int ordinal);

  @Query(
      value =
          "select p.id, p.name,p.password, p.description,p.email,p.principal_type, p.status, p.total_transaction_sum"
              + " from principal p where p.id = ?1 and p.principal_type = ?2 ",
      nativeQuery = true)
  Optional<Principal> findByIdAndPrincipalType(long id, int type);
}
