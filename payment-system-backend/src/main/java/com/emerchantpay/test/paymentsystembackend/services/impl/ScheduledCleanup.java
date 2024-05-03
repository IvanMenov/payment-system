package com.emerchantpay.test.paymentsystembackend.services.impl;

import com.emerchantpay.test.paymentsystembackend.model.Principal;
import com.emerchantpay.test.paymentsystembackend.model.Transaction;
import com.emerchantpay.test.paymentsystembackend.model.TransactionType;
import com.emerchantpay.test.paymentsystembackend.repositories.PrincipalRepository;
import com.emerchantpay.test.paymentsystembackend.repositories.TransactionRepository;
import com.emerchantpay.test.paymentsystembackend.services.IScheduledTask;
import jakarta.persistence.OptimisticLockException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class ScheduledCleanup implements IScheduledTask {

  public static final int HOURLY = 60 * 60 * 1000;

  @Autowired private TransactionRepository transactionRepository;

  @Autowired private PrincipalRepository principalRepository;

  @Autowired private TransactionTemplate transactionTemplate;

  /**
   * Delete transactions that are older than 1 hour this functionality is executed asynchronously
   * every hour. If the transaction to be deleted is of type CHARGE and status APPROVED, subtract
   * that amount from the principal total amount
   */
  @Override
  @Scheduled(fixedRate = HOURLY)
  public void runTask() {
    long olderThanTimestamp = System.currentTimeMillis() - HOURLY;
    transactionTemplate.executeWithoutResult(
        (transactionStatus) -> {
          List<Transaction> transactionList =
              transactionRepository.getTransactionsOlderThanAnHour(olderThanTimestamp);
          Map<Principal, Double> merchantToAmountToBeDecreaseFrom = new HashMap<>();
          List<String> uuidsToDelete = new ArrayList<>();

          // only if there are transactions of type CHARGE which are with status APPROVED
          // we should decrease the principal total transactions sum by subtracting
          // that amount from his total transaction sum
          transactionList.forEach(
              transaction -> {
                uuidsToDelete.add(transaction.getUuid());
                if (transaction.getType() == TransactionType.CHARGE
                    && transaction.getStatus() == Transaction.Status.APPROVED) {
                  if (merchantToAmountToBeDecreaseFrom.get(transaction.getMerchant()) != null) {
                    double newAmount =
                        merchantToAmountToBeDecreaseFrom.get(transaction.getMerchant())
                            + transaction.getAmount();
                    merchantToAmountToBeDecreaseFrom.put(transaction.getMerchant(), newAmount);
                  } else {
                    merchantToAmountToBeDecreaseFrom.put(
                        transaction.getMerchant(), transaction.getAmount());
                  }
                }
              });

          if (!uuidsToDelete.isEmpty()) {
            transactionRepository.deleteTransactionsOlderThan(uuidsToDelete);
            merchantToAmountToBeDecreaseFrom.forEach(
                (merchant, amountToDecrease) -> {
                  boolean isUpdated = false;
                  Principal principal =
                      principalRepository.findPrincipalById(merchant.getId()).get();
                  while (!isUpdated) {
                    try {
                      principal.updateTotalTransactionSum(amountToDecrease, false);
                      principalRepository.save(principal);
                      isUpdated = true;
                    } catch (OptimisticLockException exception) {
                      principal = principalRepository.findPrincipalById(merchant.getId()).get();
                    }
                  }
                });
          }
        });
  }
}
