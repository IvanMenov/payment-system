package com.emerchantpay.test.paymentsystembackend.controllers.v1;

import com.emerchantpay.test.paymentsystembackend.model.JwtTokenResponse;
import com.emerchantpay.test.paymentsystembackend.model.Principal;
import com.emerchantpay.test.paymentsystembackend.model.SigninRequest;
import com.emerchantpay.test.paymentsystembackend.services.IAuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {
  @Autowired private IAuthenticationService authenticationService;

  @PostMapping(value = "/signin")
  public ResponseEntity<JwtTokenResponse> signin(@RequestBody SigninRequest request) {
    return ResponseEntity.ok(authenticationService.signin(request));
  }

  @GetMapping(path = "/whoami")
  public Principal userMe(Authentication authentication) {
    return (Principal) authentication.getPrincipal();
  }
}
