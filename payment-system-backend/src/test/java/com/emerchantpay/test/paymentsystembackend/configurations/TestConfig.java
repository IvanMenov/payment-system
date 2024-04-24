package com.emerchantpay.test.paymentsystembackend.configurations;

import com.emerchantpay.test.paymentsystembackend.model.Principal;
import com.emerchantpay.test.paymentsystembackend.repositories.PrincipalRepository;
import com.emerchantpay.test.paymentsystembackend.services.impl.PrincipalUserDetailsService;
import java.util.Optional;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@TestConfiguration
public class TestConfig {

  @MockBean PrincipalRepository principalRepository;

  @Bean
  @Primary
  public PrincipalUserDetailsService userDetailsService() {
    Principal principal =
        Principal.builder()
            .email("testing@gmail.com")
            .password(passwordEncoder().encode("pass"))
            .build();
    Mockito.when(principalRepository.findByEmail("testing@gmail.com"))
        .thenReturn(Optional.of(principal));
    return new PrincipalUserDetailsService();
  }

  @Bean
  public AuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService());
    authProvider.setPasswordEncoder(passwordEncoder());
    return authProvider;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
      throws Exception {
    return config.getAuthenticationManager();
  }
}
