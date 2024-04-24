package com.emerchantpay.test.paymentsystembackend.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SigninRequest {
  @NotNull @Email private String email;

  @NotNull private String password;
}
