package com.emerchantpay.paymentsystembackend.model;

public enum TransactionStatus {
    APPROVED("approved"),
    REVERSED("reversed"),
    REFUNDED("refunded"),
    ERROR("error");

    public final String trStatus;

    TransactionStatus(String status) {
        this.trStatus = status;
    }

    public String getStatusType() {
        return this.trStatus;
    }
}
