package com.emerchantpay.test.paymentsystembackend.repositories;

import com.emerchantpay.test.paymentsystembackend.model.Principal;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MerchantRepository extends JpaRepository<Principal, Long> {
  Optional<Principal> findByEmail(String email);
}
