package com.emerchantpay.paymentsystembackend.model;

public enum TransactionType {
  AUTHORIZE("authorize"),
  CHARGE("charge"),
  REFUND("refund"),
  REVERSAL("reversal");

  private String transactionType;


  TransactionType(String transactionType) {
    this.transactionType = transactionType;
  }

  public String getType() {
    return this.transactionType;
  }


}
