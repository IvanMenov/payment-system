package com.emerchantpay.test.paymentsystembackend.utils;

import com.emerchantpay.test.paymentsystembackend.model.Payment;
import com.emerchantpay.test.paymentsystembackend.model.Principal;
import com.emerchantpay.test.paymentsystembackend.model.Transaction;
import com.emerchantpay.test.paymentsystembackend.model.TransactionType;
import java.util.UUID;

public class TransactionFactory {
  public static Transaction createTransaction(
      Principal merchant, Payment payment, Transaction authorizeTransaction) {
    Transaction createdTransaction = null;
    Transaction.TransactionBuilder transactionBuilder =
        Transaction.builder()
            .customerEmail(payment.getCustomer().getCustomerEmail())
            .customerPhone(payment.getCustomer().getCustomerPhone())
            .uuid(payment.getUuid() == null ? UUID.randomUUID().toString() : payment.getUuid())
            .merchant(merchant)
            .timestamp(System.currentTimeMillis());
    if (payment.getTransactionType() == TransactionType.REVERSAL) {
      if (payment.getReferenceId() == null) {
        throw new RuntimeException(
            "Reversal transaction should have referenceId  pointing to authorization transaction!");
      }
      createdTransaction =
          transactionBuilder
              .type(TransactionType.REVERSAL)
              // should reference authorization transaction
              .referenceTransactionUUID(payment.getReferenceId())
              .build();
    } else if (payment.getTransactionType() == TransactionType.CHARGE) {
      if (authorizeTransaction != null) {
        // there should already be an authorizationion transaction
        createdTransaction =
            transactionBuilder
                .amount(payment.getAmount())
                .type(TransactionType.CHARGE)
                .referenceTransactionUUID(authorizeTransaction.getUuid())
                .build();
      } else {
        // first create authorization transaction
        createdTransaction =
            transactionBuilder.amount(payment.getAmount()).type(TransactionType.AUTHORIZE).build();
      }
    } else if (payment.getTransactionType() == TransactionType.REFUND) {
      if (payment.getReferenceId() == null) {
        throw new RuntimeException(
            "Refund transaction should have referenceId pointing to charge transaction!");
      }
      createdTransaction =
          transactionBuilder
              .amount(payment.getAmount())
              .type(TransactionType.REFUND)
              // should reference charge transaction
              .referenceTransactionUUID(payment.getReferenceId())
              .build();
    }

    return createdTransaction;
  }
}
