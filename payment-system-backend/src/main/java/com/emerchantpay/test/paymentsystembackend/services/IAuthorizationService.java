package com.emerchantpay.test.paymentsystembackend.services;

public interface IAuthorizationService {

  boolean authorizeAdmin();

  boolean matchAuthenticatedPrincipalWithId(String id);

  boolean authorizeMerchant();
}
