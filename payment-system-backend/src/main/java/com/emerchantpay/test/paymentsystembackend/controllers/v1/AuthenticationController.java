package com.emerchantpay.test.paymentsystembackend.controllers.v1;

import com.emerchantpay.test.paymentsystembackend.model.JwtAuthenticationResponse;
import com.emerchantpay.test.paymentsystembackend.model.SigninRequest;
import com.emerchantpay.test.paymentsystembackend.services.IAuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {
  @Autowired private IAuthenticationService authenticationService;

  @PostMapping(value = "/signin")
  public ResponseEntity<JwtAuthenticationResponse> signin(@RequestBody SigninRequest request) {
    return ResponseEntity.ok(authenticationService.signin(request));
  }
}
