package com.emerchantpay.test.paymentsystembackend.services;

import com.emerchantpay.test.paymentsystembackend.model.JwtTokenResponse;
import com.emerchantpay.test.paymentsystembackend.model.SigninRequest;

public interface IAuthenticationService {

  public JwtTokenResponse signin(SigninRequest request);

  public String encodePassword(String password);
}
