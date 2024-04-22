package com.emerchantpay.test.paymentsystembackend.services.impl;

import com.emerchantpay.test.paymentsystembackend.model.JwtAuthenticationResponse;
import com.emerchantpay.test.paymentsystembackend.model.SigninRequest;
import com.emerchantpay.test.paymentsystembackend.services.IAuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService implements IAuthenticationService {

  @Autowired private JwtService jwtService;

  @Autowired private AuthenticationManager authenticationManager;

  @Autowired private UserDetailsService service;

  @Autowired private PasswordEncoder passwordEncoder;

  @Override
  public JwtAuthenticationResponse signin(SigninRequest request) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
    UserDetails principal = service.loadUserByUsername(request.getEmail());
    if (principal == null) {
      throw new IllegalArgumentException("Invalid email or password.");
    }
    var jwt = jwtService.generateToken(principal);
    return JwtAuthenticationResponse.builder().token(jwt).build();
  }

  @Override
  public String encodePassword(String password) {
    return passwordEncoder.encode(password);
  }
}
