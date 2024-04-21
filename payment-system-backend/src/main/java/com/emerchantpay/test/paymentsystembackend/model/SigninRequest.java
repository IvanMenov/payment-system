package com.emerchantpay.test.paymentsystembackend.model;

import lombok.Data;

@Data
public class SigninRequest {
  private String email;
  private String password;
}
