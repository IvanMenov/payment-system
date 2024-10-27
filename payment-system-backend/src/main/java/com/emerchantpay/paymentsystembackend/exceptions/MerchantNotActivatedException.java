package com.emerchantpay.paymentsystembackend.exceptions;

import java.io.Serial;

public class MerchantNotActivatedException extends RuntimeException {
  @Serial private static final long serialVersionUID = 1L;

  private static final String MERCHANT_NOT_ACTIVE =
      "Merchant: %s is not activated and cannot process transactions!";

  public MerchantNotActivatedException(String merchant) {
    super(String.format(MERCHANT_NOT_ACTIVE, merchant));
  }
}
