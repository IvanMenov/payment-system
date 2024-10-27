package com.emerchantpay.paymentsystembackend.security;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "jwt.auth.converter")
public class JwtConverterProperties {
  @NotBlank
  // authorized party/realm
  private String resourceId;

  private String principalAttribute;
}
