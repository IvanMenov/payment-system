package com.emerchantpay.test.paymentsystembackend.exceptions;

public class PaymentTypeNotAllowedException extends RuntimeException {

  private static final String PAYMENT_TYPE_NOT_ALLOWED = "Payment of type: %s is not allowed!";

  public PaymentTypeNotAllowedException(String message) {
    super(String.format(PAYMENT_TYPE_NOT_ALLOWED, message));
  }
}
