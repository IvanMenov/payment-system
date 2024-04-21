package com.emerchantpay.test.paymentsystembackend.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.validator.constraints.UUID;

@Data
public class Payment {
  @UUID private String uuid;

  @Positive(message = "transaction amount must be greater than 0")
  private Double amount;

  @Positive private Customer customer;

  private TransactionType transactionType;

  private String referenceId;

  @Data
  public static class Customer {
    @Email(message = "Email should be valid")
    private String customerEmail;

    @Size(min = 10, max = 15, message = "phone number must be between 10 and 15 characters")
    // https://regex101.com/r/j48BZs/2 -pattern for matching various phone formats
    @Pattern(regexp = "^(\\+\\d{1,2}\s?)?\\(?\\d{3}\\)?[\s.-]?\\d{3}[\s.-]?\\d{4}$")
    private String customerPhone;

    @Positive(message = "customer's amount must be greater than 0")
    private Double customerAmount;
  }
}
