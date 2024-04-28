package com.emerchantpay.test.paymentsystembackend.services;

import java.util.List;
import java.util.Optional;

public interface IPrincipalService<T> {

  void deleteMerchantById(long id);

  public boolean isMerchantInactive(T merchant);

  public T createOrUpdatePrincipal(T merchant);

  List<T> findAllMerchants();

  Optional<T> findPrincipalById(long id);
}
