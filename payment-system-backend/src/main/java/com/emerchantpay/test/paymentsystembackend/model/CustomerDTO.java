package com.emerchantpay.test.paymentsystembackend.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "customer")
public class CustomerDTO {
  @Email(message = "Email should be valid")
  private String customerEmail;

  @Size(min = 10, max = 15, message = "phone number must be between 10 and 15 characters")
  // https://regex101.com/r/j48BZs/2 -pattern for matching various phone formats
  @Pattern(regexp = "^(\\+\\d{1,2}\s?)?\\(?\\d{3}\\)?[\s.-]?\\d{3}[\s.-]?\\d{4}$")
  private String customerPhone;

  @Positive(message = "customer's amount must be greater than 0")
  private Double customerAmount;

  public String getCustomerEmail() {
    return customerEmail;
  }

  public void setCustomerEmail(String customerEmail) {
    this.customerEmail = customerEmail;
  }

  public String getCustomerPhone() {
    return customerPhone;
  }

  public void setCustomerPhone(String customerPhone) {
    this.customerPhone = customerPhone;
  }

  public Double getCustomerAmount() {
    return customerAmount;
  }

  public void setCustomerAmount(Double customerAmount) {
    this.customerAmount = customerAmount;
  }
}
