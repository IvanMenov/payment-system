package com.emerchantpay.test.paymentsystembackend.services.impl;

import com.emerchantpay.test.paymentsystembackend.model.JwtTokenResponse;
import com.emerchantpay.test.paymentsystembackend.model.Principal;
import com.emerchantpay.test.paymentsystembackend.model.SigninRequest;
import com.emerchantpay.test.paymentsystembackend.services.IAuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService implements IAuthenticationService {

  @Autowired private JwtService jwtService;

  @Autowired private AuthenticationManager authenticationManager;

  @Autowired private PasswordEncoder passwordEncoder;

  /**
   * @param request upon successful sign in the service returns a valid jwt token for the user that
   *     signed in, that is valid for 60 minutes by default the expiration time of the token can be
   *     changed by changing the value for {app.jwt.expiration.min} in application-dev.properties
   *     file
   * @return JwtAuthenticationResponse
   */
  @Override
  public JwtTokenResponse signin(SigninRequest request) {
    Authentication authentication =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
    // principal should be set by the authentication manager
    Principal principal = (Principal) authentication.getPrincipal();
    String jwt = jwtService.generateToken(principal);
    return JwtTokenResponse.builder().token(jwt).build();
  }

  /**
   * @param password encoded the password before saving in the database the password encoder used is
   *     BCryptPasswordEncoder
   * @return String
   */
  @Override
  public String encodePassword(String password) {
    return passwordEncoder.encode(password);
  }
}
