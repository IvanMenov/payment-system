package com.emerchantpay.test.paymentsystembackend.services;

import com.emerchantpay.test.paymentsystembackend.model.JwtAuthenticationResponse;
import com.emerchantpay.test.paymentsystembackend.model.SigninRequest;

public interface IAuthenticationService {

  public JwtAuthenticationResponse signin(SigninRequest request);
}
