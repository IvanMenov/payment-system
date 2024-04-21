package com.emerchantpay.test.paymentsystembackend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

  @Id private String uuid;

  private double amount;

  @Column(name = "customer_email")
  private String customerEmail;

  @Column(name = "customer_phone")
  private String customerPhone;

  @Column(name = "reference_transaction_uuid")
  private String referenceTransactionUUID;

  @ManyToOne
  @JoinColumn(name = "principal_id")
  private Principal merchant;

  private Status status;

  private TransactionType type;

  private long timestamp;

  public enum Status {
    APPROVED("approved"),
    REVERSED("reversed"),
    REFUNDED("refunded"),
    ERROR("error");

    private final String trStatus;

    private Status(String status) {
      this.trStatus = status;
    }
  }
}
