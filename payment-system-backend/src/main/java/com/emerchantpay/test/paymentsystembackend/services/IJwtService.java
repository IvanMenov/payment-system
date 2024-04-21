package com.emerchantpay.test.paymentsystembackend.services;

import org.springframework.security.core.userdetails.UserDetails;

public interface IJwtService {
  public String extractUserName(String token);

  public String generateToken(UserDetails userDetails);

  public boolean isTokenValid(String token, UserDetails userDetails);
}
