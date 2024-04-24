package com.emerchantpay.test.paymentsystembackend.model;

import jakarta.validation.constraints.*;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.hibernate.validator.constraints.UUID;

@XmlRootElement(name = "payment")
public class PaymentDTO {
  @UUID private String uuid;

  @Positive(message = "transaction amount must be greater than 0")
  private Double amount;

  private CustomerDTO customer;

  @NotNull private TransactionType transactionType;

  private String referenceId;

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public Double getAmount() {
    return amount;
  }

  public void setAmount(Double amount) {
    this.amount = amount;
  }

  public CustomerDTO getCustomer() {
    return customer;
  }

  public void setCustomer(CustomerDTO customer) {
    this.customer = customer;
  }

  public TransactionType getTransactionType() {
    return transactionType;
  }

  public void setTransactionType(TransactionType transactionType) {
    this.transactionType = transactionType;
  }

  public String getReferenceId() {
    return referenceId;
  }

  public void setReferenceId(String referenceId) {
    this.referenceId = referenceId;
  }
}
