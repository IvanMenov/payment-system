package com.emerchantpay.paymentsystembackend.services;

import com.emerchantpay.paymentsystembackend.model.Payment;
import com.emerchantpay.paymentsystembackend.model.Principal;
import com.emerchantpay.paymentsystembackend.model.Transaction;

import java.util.Optional;

public interface IPaymentService {

  boolean isTransactionAlreadySubmitted(Payment payment);

  Transaction initializeTransaction(Principal merchant, Payment payment);

  void commenceTransactionProcess(
          Principal merchant, Payment payment, Transaction initialTransaction);

  Optional<Transaction> getTransactionInformation(String uuid);

  boolean hasNoReferenceId(Payment payment);
}
