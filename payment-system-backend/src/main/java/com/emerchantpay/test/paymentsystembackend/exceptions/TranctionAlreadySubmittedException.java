package com.emerchantpay.test.paymentsystembackend.exceptions;

import java.io.Serial;

public class TranctionAlreadySubmittedException extends RuntimeException {

  @Serial private static final long serialVersionUID = 1L;

  private static final String MERCHANT_NOT_ACTIVE = "Transaction: %s already exist!";

  public TranctionAlreadySubmittedException(String uuid) {
    super(String.format(MERCHANT_NOT_ACTIVE, uuid));
  }
}
