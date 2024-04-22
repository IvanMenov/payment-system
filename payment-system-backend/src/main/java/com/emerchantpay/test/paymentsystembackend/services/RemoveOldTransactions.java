package com.emerchantpay.test.paymentsystembackend.services;

import com.emerchantpay.test.paymentsystembackend.repositories.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RemoveOldTransactions {

  public static final int HOURLY = 60 * 60 * 1000;

  @Autowired private TransactionRepository transactionRepository;

  @Scheduled(fixedRate = HOURLY)
  public void removeOldTransaction() {
    long olderThanTimestamp = System.currentTimeMillis() - HOURLY;
    transactionRepository.deleteTransactionsOlderThan(olderThanTimestamp);
  }
}
