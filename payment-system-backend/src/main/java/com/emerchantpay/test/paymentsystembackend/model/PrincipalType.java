package com.emerchantpay.test.paymentsystembackend.model;

public enum PrincipalType {
  ADMIN("admin"),
  MERCHANT("merchant");

  private String type;

  PrincipalType(String prType) {
    this.type = prType;
  }

  public String getType() {
    return type;
  }
}
