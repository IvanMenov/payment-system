package com.emerchantpay.paymentsystembackend.model;

public enum PrincipalType {
  ADMIN("admin"),
  MERCHANT("merchant");

  private String prType;

  PrincipalType(String prType) {
    this.prType = prType;
  }

  public String getType() {
    return prType;
  }
}
