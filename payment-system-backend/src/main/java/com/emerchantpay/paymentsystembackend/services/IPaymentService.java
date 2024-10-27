package com.emerchantpay.paymentsystembackend.services;

import com.emerchantpay.paymentsystembackend.model.PaymentDTO;
import com.emerchantpay.paymentsystembackend.model.Principal;
import com.emerchantpay.paymentsystembackend.model.Transaction;
import java.util.List;
import java.util.Optional;

public interface IPaymentService {

  boolean isTransactionAlreadySubmitted(PaymentDTO payment);

  Transaction initializeTransaction(Principal merchant, PaymentDTO payment);

  void commenceTransactionProcess(
      Principal merchant, PaymentDTO payment, Transaction initialTransaction);

  Optional<Transaction> getTransactionInformation(String uuid);

  boolean hasNoReferenceId(PaymentDTO payment);
}
