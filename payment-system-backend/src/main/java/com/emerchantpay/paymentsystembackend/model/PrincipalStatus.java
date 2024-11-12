package com.emerchantpay.paymentsystembackend.model;

public enum PrincipalStatus {
    INACTIVE("inactive"),
    ACTIVE("active");
    private final String principalStatus;

    private PrincipalStatus(String principalStatus) {
        this.principalStatus = principalStatus;
    }

    public String getStatusValue() {
        return this.principalStatus;
    }
}
