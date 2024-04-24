package com.emerchantpay.test.paymentsystembackend.services;

import com.emerchantpay.test.paymentsystembackend.model.JwtAuthenticationResponse;
import com.emerchantpay.test.paymentsystembackend.model.SigninRequest;

public interface IAuthenticationService {

  public JwtAuthenticationResponse signin(SigninRequest request);

  /**
   * @param password encoded the password before saving in the database the password encoder used is
   *     BCryptPasswordEncoder
   * @return String
   */
  public String encodePassword(String password);
}
