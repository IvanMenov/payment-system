package com.emerchantpay.paymentsystembackend.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JwtTokenResponse {
  private String token;
}
