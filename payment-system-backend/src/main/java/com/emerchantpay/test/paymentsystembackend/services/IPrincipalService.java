package com.emerchantpay.test.paymentsystembackend.services;

import com.emerchantpay.test.paymentsystembackend.model.Principal;
import java.util.List;
import java.util.Optional;

public interface IPrincipalService {

  public boolean isMerchantInactive(Principal merchant);

  public void createOrUpdatePrincipal(Principal merchant);

  public List<Principal> findAllAdmins();

  List<Principal> findAllMerchants();

  Optional<Principal> findPrincipalById(long id);

  Optional<Principal> findPrincipalByEmail(String email);
}
