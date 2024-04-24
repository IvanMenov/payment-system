package com.emerchantpay.test.paymentsystembackend.exceptions;

public class NoReferenceIdException extends RuntimeException {

  private static final String REQUIRED_FOR_REVERSAL_OR_REFUND_TRANSACTIONS =
      "ReferenceId is required for REVERSAL OR REFUND transactions!";

  public NoReferenceIdException(String message) {
    super(String.format(REQUIRED_FOR_REVERSAL_OR_REFUND_TRANSACTIONS, message));
  }
}
