package com.emerchantpay.test.paymentsystembackend.repositories;

import com.emerchantpay.test.paymentsystembackend.model.Principal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PrincipalRepository extends JpaRepository<Principal, Long> {
  Optional<Principal> findByEmail(String email);

  @Query(
      value =
          "select p.id, p.name, p.discription,p.email,p.principal_type, p.status"
              + " from principal p where p.principal_type = ?1",
      nativeQuery = true)
  List<Principal> findAllByPrincipalType(String type);

  @Query(
      value =
          "select p.id, p.name, p.discription,p.email,p.principal_type, p.status"
              + " from principal p where p.id = ?1 and p.principal_type = ?2 ",
      nativeQuery = true)
  Optional<Principal> findByIdAndPrincipalType(long id, String type);
}
