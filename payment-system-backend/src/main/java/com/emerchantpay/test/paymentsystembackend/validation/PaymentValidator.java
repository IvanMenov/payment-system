package com.emerchantpay.test.paymentsystembackend.validation;

import com.emerchantpay.test.paymentsystembackend.model.CustomerDTO;
import com.emerchantpay.test.paymentsystembackend.model.PaymentDTO;
import com.emerchantpay.test.paymentsystembackend.model.TransactionType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PaymentValidator implements ConstraintValidator<Payment, PaymentDTO> {

  @Override
  public boolean isValid(
      PaymentDTO paymentDTO, ConstraintValidatorContext constraintValidatorContext) {
    if (paymentDTO.getTransactionType() == TransactionType.CHARGE) {
      return checkAmount(paymentDTO.getAmount())
          && checkCustomerAmount(paymentDTO.getCustomer(), paymentDTO.getAmount())
          && checkCustomerEmail(paymentDTO.getCustomer())
          && checkCustomerPhone(paymentDTO.getCustomer());

    } else if (paymentDTO.getTransactionType() == TransactionType.REFUND) {

      return checkAmount(paymentDTO.getAmount()) && checkReferenceUUID(paymentDTO.getReferenceId());
    } else if (paymentDTO.getTransactionType() == TransactionType.REVERSAL) {
      return checkReferenceUUID(paymentDTO.getReferenceId());
    }
    return false;
  }

  private boolean checkReferenceUUID(String uuid) {
    return uuid != null;
  }

  private boolean checkCustomerPhone(CustomerDTO customerDTO) {
    return customerDTO.getCustomerPhone() != null
        && PhoneValidation.patternMatches(customerDTO.getCustomerPhone());
  }

  private boolean checkCustomerEmail(CustomerDTO customerDTO) {
    return customerDTO.getCustomerEmail() != null
        && EmailValidation.patternMatches(customerDTO.getCustomerEmail());
  }

  private boolean checkCustomerAmount(CustomerDTO customer, Double amount) {
    return customer != null && customer.getCustomerAmount() != null;
  }

  private boolean checkAmount(Double amount) {
    return amount != null && amount > 0;
  }
}
