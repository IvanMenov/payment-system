package com.emerchantpay.test.paymentsystembackend.services.impl;

import com.emerchantpay.test.paymentsystembackend.model.Transaction;
import com.emerchantpay.test.paymentsystembackend.repositories.TransactionRepository;
import com.emerchantpay.test.paymentsystembackend.services.IScheduledTask;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class ScheduledCleanup implements IScheduledTask {

  public static final int HOURLY = 60 * 60 * 1000;

  @Autowired private TransactionRepository transactionRepository;

  @Autowired private TransactionTemplate transactionTemplate;

  /**
   * delete transactions that are older than 1 hour this functionality is executed asynchronously
   * every hour
   */
  @Override
  @Scheduled(fixedRate = HOURLY)
  public void runTask() {
    long olderThanTimestamp = System.currentTimeMillis() - HOURLY;
    transactionTemplate.executeWithoutResult(
        (transactionStatus) -> {
          List<String> uuidsToDelete =
              transactionRepository.getTransactionsOlderThanAnHour(olderThanTimestamp).stream()
                  .map(Transaction::getUuid)
                  .collect(Collectors.toList());
          if (!uuidsToDelete.isEmpty()) {
            transactionRepository.deleteTransactionsOlderThan(uuidsToDelete);
          }
        });
  }
}
