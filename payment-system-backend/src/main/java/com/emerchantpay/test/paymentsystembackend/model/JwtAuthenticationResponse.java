package com.emerchantpay.test.paymentsystembackend.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JwtAuthenticationResponse {
  private String token;
}
