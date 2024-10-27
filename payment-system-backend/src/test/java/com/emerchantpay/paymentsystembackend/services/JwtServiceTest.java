/*
package com.emerchantpay.test.paymentsystembackend.services;

import static org.junit.jupiter.api.Assertions.*;

import com.emerchantpay.test.paymentsystembackend.constants.Constants;
import com.emerchantpay.test.paymentsystembackend.model.Principal;
import com.emerchantpay.test.paymentsystembackend.services.impl.JwtService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;

@WebMvcTest(JwtServiceTest.class)
@Import({TestConfig.class, JwtService.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JwtServiceTest {
  private static final String EXPECTED_EMAIL = "testing@gmail.com";

  @Autowired private IJwtService jwtService;

  private static String token = null;

  private static Principal userDetails = null;

  @Test
  @Order(1)
  public void testGenerateToken() {
    Principal principal =
        Principal.builder().email(Constants.MAIL).password(Constants.PASS).build();
    String jwt = jwtService.generateToken(principal);
    assertNotNull(jwt);
    token = jwt;
    userDetails = principal;
  }

  @Test
  @Order(3)
  public void testExtractUserName() {
    String email = jwtService.extractUserName(token);
    assertNotNull(email);
    assertEquals(EXPECTED_EMAIL, email);
  }

  @Test
  @Order(2)
  public void testIsTokenValid() {
    assertTrue(jwtService.isTokenValid(token, userDetails));
  }
}
*/
