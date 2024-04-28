package com.emerchantpay.test.paymentsystembackend.services;

import static com.emerchantpay.test.paymentsystembackend.constants.Constants.MAIL;
import static com.emerchantpay.test.paymentsystembackend.constants.Constants.PASS;
import static org.junit.jupiter.api.Assertions.*;

import com.emerchantpay.test.paymentsystembackend.configurations.TestConfig;
import com.emerchantpay.test.paymentsystembackend.model.JwtTokenResponse;
import com.emerchantpay.test.paymentsystembackend.model.SigninRequest;
import com.emerchantpay.test.paymentsystembackend.services.impl.AuthenticationService;
import com.emerchantpay.test.paymentsystembackend.services.impl.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.crypto.password.PasswordEncoder;

// @SpringBootTest
@WebMvcTest(AuthenticationService.class)
@Import({JwtService.class, TestConfig.class})
class AuthenticationServiceTest {

  @Autowired AuthenticationService authenticationService;
  @Autowired public PasswordEncoder passwordEncoder;

  @Test
  public void testPasswordEncode() {
    String encoded = authenticationService.encodePassword(PASS);
    assertTrue(passwordEncoder.matches(PASS, encoded));
  }

  @Test
  void testSignIn() {
    // this attempt should succeed
    SigninRequest signinRequestSuccess = new SigninRequest(MAIL, PASS);
    JwtTokenResponse responseSuccess = authenticationService.signin(signinRequestSuccess);

    assertNotNull(responseSuccess);
    assertNotNull(responseSuccess.getToken());

    // the next two attempts should fail
    SigninRequest wrondPass = new SigninRequest(MAIL, "pass1");
    assertThrows(BadCredentialsException.class, () -> authenticationService.signin(wrondPass));

    SigninRequest wrongEmail = new SigninRequest("testing1@gmail.com", PASS);
    assertThrows(
        InternalAuthenticationServiceException.class,
        () -> authenticationService.signin(wrongEmail));
  }
}
