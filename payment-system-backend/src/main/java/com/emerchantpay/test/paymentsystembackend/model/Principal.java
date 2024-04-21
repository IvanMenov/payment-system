package com.emerchantpay.test.paymentsystembackend.model;

import com.google.common.util.concurrent.AtomicDouble;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "principal")
public class Principal implements UserDetails {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;
  private String description;

  @Email(message = "Email should be valid")
  @NotBlank
  private String email;

  @NotBlank private String password;

  private Status status;

  @Column(name = "total_transaction_sum")
  private AtomicDouble totalTransactionSum;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "merchant", cascade = CascadeType.ALL)
  private List<Transaction> transactionList;

  @Column(name = "principal_type")
  @NotBlank
  private PrincipalType principalType;

  public void addTransaction(Transaction transaction) {
    transactionList.add(transaction);
  }

  public boolean updateTotalTransactionSum(double amount, boolean shouldAdd) {
    double currentSum = totalTransactionSum.get();
    if (shouldAdd) {
      return totalTransactionSum.compareAndSet(currentSum, currentSum + amount);
    } else {
      return totalTransactionSum.compareAndSet(currentSum, currentSum - amount);
    }
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return new ArrayList<GrantedAuthority>();
  }

  @Override
  public String getUsername() {
    return this.email;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
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
