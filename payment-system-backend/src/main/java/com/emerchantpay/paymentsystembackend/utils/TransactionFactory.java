package com.emerchantpay.paymentsystembackend.utils;

import com.emerchantpay.paymentsystembackend.model.PaymentDTO;
import com.emerchantpay.paymentsystembackend.model.Principal;
import com.emerchantpay.paymentsystembackend.model.Transaction;
import com.emerchantpay.paymentsystembackend.model.TransactionType;
import java.util.UUID;

public class TransactionFactory {
  public static Transaction createTransaction(
      Principal merchant, PaymentDTO payment, Transaction authorizeTransaction) {
    Transaction createdTransaction = null;
    Transaction.TransactionBuilder transactionBuilder =
        Transaction.builder()
            .uuid(payment.getUuid() == null ? UUID.randomUUID().toString() : payment.getUuid())
            .merchant(merchant)
            .timestamp(System.currentTimeMillis());
    if (payment.getTransactionType() == TransactionType.REVERSAL) {
      createdTransaction =
          transactionBuilder
              .transactionType(TransactionType.REVERSAL)
              // should reference authorization transaction
              .referenceTransactionUUID(payment.getReferenceId())
              .build();
    } else if (payment.getTransactionType() == TransactionType.CHARGE) {
      if (authorizeTransaction != null) {
        // there should already be an authorizationion transaction
        createdTransaction =
            transactionBuilder
                .amount(payment.getAmount())
                .transactionType(TransactionType.CHARGE)
                .customerEmail(authorizeTransaction.getCustomerEmail())
                .customerPhone(authorizeTransaction.getCustomerPhone())
                .referenceTransactionUUID(authorizeTransaction.getUuid())
                .build();
      } else {
        // first create authorization transaction
        createdTransaction =
            transactionBuilder
                .amount(payment.getAmount())
                .transactionType(TransactionType.AUTHORIZE)
                .customerEmail(payment.getCustomer().getCustomerEmail())
                .customerPhone(payment.getCustomer().getCustomerPhone())
                .build();
      }
    } else if (payment.getTransactionType() == TransactionType.REFUND) {
      createdTransaction =
          transactionBuilder
              .amount(payment.getAmount())
              .transactionType(TransactionType.REFUND)
              // should reference charge transaction
              .referenceTransactionUUID(payment.getReferenceId())
              .build();
    } else {
      throw new IllegalArgumentException(
          String.format(
              "%s is invalid type. Valid options are: CHARGE, REFUND, REVERSAL",
              payment.getTransactionType().getType()));
    }

    return createdTransaction;
  }
}
