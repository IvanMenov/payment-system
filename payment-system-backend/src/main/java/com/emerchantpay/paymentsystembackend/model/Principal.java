package com.emerchantpay.paymentsystembackend.model;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.*;

import lombok.*;
import org.springframework.web.bind.annotation.GetMapping;

@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "principal")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Principal {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;

  private String description;

  @Email(message = "Email should be valid")
  @NotBlank
  private String email;

  @Column(name = "principal_status")
  @Enumerated(EnumType.STRING)
  private PrincipalStatus principalStatus;

  @Column(name = "total_transaction_sum", precision = 8, scale = 2)
  private BigDecimal totalTransactionSum;

  @OneToMany(mappedBy = "merchant", fetch = FetchType.LAZY)
  @JsonManagedReference
  private List<Transaction> transactionList;

  @Column(name = "principal_type")
  @Enumerated(EnumType.STRING)
  private PrincipalType principalType;

  @Transient
  private long countTransactions;

  @Version
  private Long version;

  public Principal(Long id, String name, String description, String email, PrincipalType principalType, PrincipalStatus principalStatus, BigDecimal totalTransactionSum, Long version) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.email = email;
    this.principalStatus = principalStatus;
    this.totalTransactionSum = totalTransactionSum;
    this.principalType = principalType;
    this.version = version;
  }

  public void addTransaction(Transaction transaction) {
    if(transactionList == null){
      transactionList = new ArrayList<>();
    }
    transactionList.add(transaction);
  }

  public void updateTotalTransactionSum(double amount, boolean shouldAdd) {
    if(totalTransactionSum == null){
      totalTransactionSum = BigDecimal.valueOf(0);
    }
    if (shouldAdd) {
      totalTransactionSum = totalTransactionSum.add(BigDecimal.valueOf(amount));
    } else {
      totalTransactionSum = totalTransactionSum.subtract(BigDecimal.valueOf(amount));
    }
  }

}
