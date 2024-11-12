package com.emerchantpay.paymentsystembackend.validation;

import com.emerchantpay.paymentsystembackend.model.Customer;
import com.emerchantpay.paymentsystembackend.model.Payment;
import com.emerchantpay.paymentsystembackend.model.TransactionType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PaymentValidator implements ConstraintValidator<com.emerchantpay.paymentsystembackend.validation.Payment, Payment> {

  @Override
  public boolean isValid(
          Payment paymentDTO, ConstraintValidatorContext constraintValidatorContext) {
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

  private boolean checkCustomerPhone(Customer customer) {
    return customer.getCustomerPhone() != null
        && PhoneValidation.patternMatches(customer.getCustomerPhone());
  }

  private boolean checkCustomerEmail(Customer customer) {
    return customer.getCustomerEmail() != null
        && EmailValidation.patternMatches(customer.getCustomerEmail());
  }

  private boolean checkCustomerAmount(Customer customer, Double amount) {
    return customer != null && customer.getCustomerAmount() != null;
  }

  private boolean checkAmount(Double amount) {
    return amount != null && amount > 0;
  }
}
