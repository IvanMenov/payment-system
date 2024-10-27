package com.emerchantpay.paymentsystembackend.services;

import java.util.List;
import java.util.Optional;

public interface IPrincipalService<T> {

  void deleteMerchantById(long id);

  boolean isMerchantInactive(T merchant);

  T createOrUpdatePrincipal(T merchant);

  List<T> findAllMerchants();

  Optional<T> findPrincipalByIdLoadBatchOfTransactions(long id, int limit, int offset);

  Optional<T> findPrincipalById(long id);

  Optional<T> findPrincipalByEmail(String email);
}
