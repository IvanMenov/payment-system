package com.emerchantpay.test.paymentsystembackend.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

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
public class Principal implements UserDetails {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;

  private String description;

  @Email(message = "Email should be valid")
  @NotBlank
  private String email;

  @NotBlank @JsonIgnore private String password;

  private Status status;

  @Column(name = "total_transaction_sum", precision = 8, scale = 2)
  private BigDecimal totalTransactionSum;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "merchant", cascade = CascadeType.ALL)
  private List<Transaction> transactionList;

  @Column(name = "principal_type")
  private PrincipalType principalType;

  @Version private Long version;

  public void addTransaction(Transaction transaction) {
    transactionList.add(transaction);
  }

  public void updateTotalTransactionSum(double amount, boolean shouldAdd) {
    if (shouldAdd) {
      totalTransactionSum = totalTransactionSum.add(BigDecimal.valueOf(amount));
    } else {
      totalTransactionSum = totalTransactionSum.subtract(BigDecimal.valueOf(amount));
    }
  }

  @JsonIgnore
  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return new ArrayList<GrantedAuthority>();
  }

  @JsonIgnore
  @Override
  public String getUsername() {
    return this.email;
  }

  @JsonIgnore
  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @JsonIgnore
  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @JsonIgnore
  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @JsonIgnore
  @Override
  public boolean isEnabled() {
    return true;
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
