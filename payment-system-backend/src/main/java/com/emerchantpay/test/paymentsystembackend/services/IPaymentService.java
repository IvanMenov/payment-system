package com.emerchantpay.test.paymentsystembackend.services;

import com.emerchantpay.test.paymentsystembackend.model.Payment;
import com.emerchantpay.test.paymentsystembackend.model.Principal;
import com.emerchantpay.test.paymentsystembackend.model.Transaction;
import java.util.List;
import java.util.Optional;

public interface IPaymentService {

  boolean isTransactionAlreadySubmitted(Payment payment);

  public Transaction initializeTransaction(Principal merchant, Payment payment);

  public void commenceTransactionValidations(
      Principal merchant, Payment payment, Transaction initialTransaction);

  public void performReversal(Transaction initialTransaction);

  public void preformCharging(Principal merchant, Payment payment, Transaction initialTransaction);

  void performRefunding(Principal merchant, Payment payment, Transaction initialTransaction);

  List<Transaction> getTransactionsForMerchant(Principal merchant);

  List<Transaction> getAllTransactionsGroupByMerchant();

  Optional<Transaction> getTransactionInformation(String uuid);

  boolean isPaymentTypeAllowed(Payment payment);
}
