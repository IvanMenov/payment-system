package com.emerchantpay.paymentsystembackend.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "uuid")
public class Transaction {

  @Id private String uuid;

  private Double amount;

  @Column(name = "customer_email")
  private String customerEmail;

  @Column(name = "customer_phone")
  private String customerPhone;

  @Column(name = "reference_transaction_uuid")
  private String referenceTransactionUUID;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "principal_id")
  @JsonBackReference
  private Principal merchant;

  @Column(name = "transaction_status")
  @Enumerated(EnumType.STRING)
  private TransactionStatus transactionStatus;

  @Column(name = "transaction_type")
  @Enumerated(EnumType.STRING)
  private TransactionType transactionType;

  private Long timestamp;
}
