package com.emerchantpay.test.paymentsystembackend.services;

import com.emerchantpay.test.paymentsystembackend.model.PaymentDTO;
import com.emerchantpay.test.paymentsystembackend.model.Principal;
import com.emerchantpay.test.paymentsystembackend.model.Transaction;
import java.util.List;
import java.util.Optional;

public interface IPaymentService {

  boolean isTransactionAlreadySubmitted(PaymentDTO payment);

  Transaction initializeTransaction(Principal merchant, PaymentDTO payment);

  void commenceTransactionProcess(
      Principal merchant, PaymentDTO payment, Transaction initialTransaction);

  List<Transaction> getTransactionsForMerchant(Principal merchant);

  Optional<Transaction> getTransactionInformation(String uuid);

  boolean hasNoReferenceId(PaymentDTO payment);
}
