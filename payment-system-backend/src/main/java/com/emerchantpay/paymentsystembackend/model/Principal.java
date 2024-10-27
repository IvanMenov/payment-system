package com.emerchantpay.paymentsystembackend.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.*;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "principal")
@NamedEntityGraph(
    name = "Principal.transactionList",
    attributeNodes = @NamedAttributeNode("transactionList"))
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
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
  private Status principalStatus;

  @Column(name = "total_transaction_sum", precision = 8, scale = 2)
  private BigDecimal totalTransactionSum;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "merchant")
  private List<Transaction> transactionList;

  @Column(name = "principal_type")
  @Enumerated(EnumType.STRING)
  private PrincipalType principalType;

  @Transient
  private long countTransactions;


  @Version private Long version;

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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Principal principal = (Principal) o;
    return Objects.equals(id, principal.id) && Objects.equals(email, principal.email);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, email);
  }

  public static enum Status {
    INACTIVE("inactive"),
    ACTIVE("active");
    private final String st;

    private Status(String st) {
      this.st = st;
    }

    public String getStatusValue() {
      return this.st;
    }
  }
}
