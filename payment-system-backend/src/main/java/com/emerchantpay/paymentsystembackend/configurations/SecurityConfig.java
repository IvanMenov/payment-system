package com.emerchantpay.paymentsystembackend.configurations;

import static com.emerchantpay.paymentsystembackend.utils.Constants.PAYMENT_SYSTEM_ADMIN_ROLE;
import static com.emerchantpay.paymentsystembackend.utils.Constants.PAYMENT_SYSTEM_MERCHANT_ROLE;

import com.emerchantpay.paymentsystembackend.security.JwtConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
  @Autowired private JwtConverter jwtAuthConverter;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.cors()
        .and()
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(
            authz ->
                authz
                    .requestMatchers(HttpMethod.GET, "/api/v1/admin/merchants")
                    .hasRole(PAYMENT_SYSTEM_ADMIN_ROLE)
                    .requestMatchers(HttpMethod.DELETE, "/api/v1/admin/merchants/**")
                    .hasRole(PAYMENT_SYSTEM_ADMIN_ROLE)
                    .requestMatchers(HttpMethod.GET, "/api/v1/admin/merchants/**")
                    .hasAnyRole(PAYMENT_SYSTEM_ADMIN_ROLE, PAYMENT_SYSTEM_MERCHANT_ROLE)
                    .requestMatchers(HttpMethod.GET, "/api/v1/principal/signin")
                    .hasAnyRole(PAYMENT_SYSTEM_ADMIN_ROLE, PAYMENT_SYSTEM_MERCHANT_ROLE)
                    .requestMatchers(HttpMethod.POST, "/api/v1/payment/transactions/init")
                    .hasAnyRole(PAYMENT_SYSTEM_MERCHANT_ROLE)
                    .requestMatchers(HttpMethod.PUT, "/api/v1/admin/merchants/*/status/**")
                    .hasRole(PAYMENT_SYSTEM_ADMIN_ROLE)
                    .requestMatchers(HttpMethod.POST, "/api/v1/payment/transactions/init")
                    .hasAnyRole(PAYMENT_SYSTEM_ADMIN_ROLE, PAYMENT_SYSTEM_MERCHANT_ROLE)
                    .anyRequest()
                    .authenticated());
    http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
    http.oauth2ResourceServer(
        oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter)));

    return http.build();
  }
}
