package com.emerchantpay.test.paymentsystembackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class PaymentSystemBackendApplication {

  public static void main(String[] args) {
    SpringApplication.run(PaymentSystemBackendApplication.class, args);
  }
}
