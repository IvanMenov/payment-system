package com.emerchantpay.test.paymentsystembackend.services.impl;

import com.emerchantpay.test.paymentsystembackend.model.Payment;
import com.emerchantpay.test.paymentsystembackend.model.Principal;
import com.emerchantpay.test.paymentsystembackend.model.Transaction;
import com.emerchantpay.test.paymentsystembackend.model.TransactionType;
import com.emerchantpay.test.paymentsystembackend.repositories.MerchantRepository;
import com.emerchantpay.test.paymentsystembackend.repositories.TransactionRepository;
import com.emerchantpay.test.paymentsystembackend.services.IPaymentService;
import com.emerchantpay.test.paymentsystembackend.utils.TransactionFactory;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class PaymentService implements IPaymentService {
  private static final long WAIT_TIME = 1 * 60 * 1000;

  @Autowired private TransactionRepository transactionRepository;

  @Autowired private MerchantRepository merchantRepository;

  @Autowired private TransactionTemplate transactionTemplate;

  @Override
  public boolean isTransactionAlreadySubmitted(Payment payment) {
    if (payment.getUuid() == null) {
      return false;
    }
    return transactionRepository.findById(payment.getUuid()).isPresent();
  }

  @Override
  public Transaction initializeTransaction(Principal merchant, Payment payment) {
    return transactionTemplate.execute(
        status -> {
          Transaction initializedTransaction =
              TransactionFactory.createTransaction(merchant, payment, null);
          merchant.addTransaction(initializedTransaction);
          merchantRepository.save(merchant);
          transactionRepository.save(initializedTransaction);
          return initializedTransaction;
        });
  }

  @Override
  @Async
  public void commenceTransactionValidations(
      Principal merchant, Payment payment, Transaction initialTransaction) {
    if (payment.getTransactionType() == TransactionType.REVERSAL) {
      performReversal(initialTransaction);
    } else if (payment.getTransactionType() == TransactionType.REFUND) {
      performRefunding(merchant, payment, initialTransaction);
    } else if (payment.getTransactionType() == TransactionType.CHARGE) {
      preformCharging(merchant, payment, initialTransaction);
    }
  }

  @Override
  public void performReversal(Transaction initialTransaction) {
    // find the authorization transaction first;
    transactionTemplate.executeWithoutResult(
        (transactionStatus) -> {
          Transaction authTransaction = null;
          Optional<Transaction> authorizationTransaction =
              transactionRepository.findById(initialTransaction.getReferenceTransactionUUID());
          if (authorizationTransaction.isPresent()
              && waitForReversalTransaction(authorizationTransaction.get()) < WAIT_TIME) {
            authTransaction = authorizationTransaction.get();
            if (authTransaction.getType() == TransactionType.AUTHORIZE) {
              if (authTransaction.getStatus() == Transaction.Status.APPROVED) {
                authTransaction.setStatus(Transaction.Status.REVERSED);
              } else {
                initialTransaction.setStatus(Transaction.Status.ERROR);
              }
            } else {
              initialTransaction.setStatus(Transaction.Status.ERROR);
            }
          } else {
            initialTransaction.setStatus(Transaction.Status.ERROR);
          }
          if (authTransaction != null) {
            // small optimization
            transactionRepository.saveAll(List.of(authTransaction, initialTransaction));
          } else {
            transactionRepository.save(initialTransaction);
          }
        });
  }

  @Override
  public void preformCharging(Principal merchant, Payment payment, Transaction initialTransaction) {
    transactionTemplate.executeWithoutResult(
        (transactionStatus) -> {
          // initialTransaction is of type AUTHORIZE
          if (authorizeTransaction(payment, initialTransaction)) {
            Transaction chargeTransaction =
                TransactionFactory.createTransaction(merchant, payment, initialTransaction);
            merchant.addTransaction(chargeTransaction);
            long time = waitForReversalTransaction(initialTransaction);
            while (time < WAIT_TIME) {
              try {
                TimeUnit.MILLISECONDS.sleep(WAIT_TIME - time);
                time = waitForReversalTransaction(initialTransaction);
              } catch (InterruptedException e) {
                throw new RuntimeException(e);
              }
            }
            // check if in the meantime there was a reversal transaction which would change the
            // status
            // of authorization transaction from Approved to Reversed
            if (transactionRepository.findById(initialTransaction.getUuid()).get().getStatus()
                == Transaction.Status.APPROVED) {
              boolean shouldRetry = false;
              do {
                shouldRetry = merchant.updateTotalTransactionSum(payment.getAmount(), true);
              } while (!shouldRetry);
              chargeTransaction.setStatus(Transaction.Status.APPROVED);
              // set reference of the authorization transaction to charge transaction
              initialTransaction.setReferenceTransactionUUID(chargeTransaction.getUuid());
              transactionRepository.save(initialTransaction);
              merchantRepository.save(merchant);
            } else {
              chargeTransaction.setStatus(Transaction.Status.ERROR);
            }
            transactionRepository.save(chargeTransaction);
          }
        });
  }

  @Override
  public void performRefunding(
      Principal merchant, Payment payment, Transaction initialTransaction) {
    transactionTemplate.executeWithoutResult(
        (transactionStatus) -> {
          // find charge transaction
          Transaction chargeTransaction = null;
          Optional<Transaction> chargeTransactionOptional =
              transactionRepository.findById(initialTransaction.getReferenceTransactionUUID());
          if (chargeTransactionOptional.isPresent()) {
            chargeTransaction = chargeTransactionOptional.get();
            if (chargeTransaction.getType() == TransactionType.CHARGE) {
              if (chargeTransaction.getStatus() == Transaction.Status.APPROVED) {
                boolean shouldRetry = false;
                do {
                  shouldRetry = merchant.updateTotalTransactionSum(payment.getAmount(), false);
                } while (!shouldRetry);
                initialTransaction.setStatus(Transaction.Status.APPROVED);
                merchantRepository.save(merchant);
              } else {
                initialTransaction.setStatus(Transaction.Status.ERROR);
              }
            } else {
              initialTransaction.setStatus(Transaction.Status.ERROR);
            }
          } else {
            initialTransaction.setStatus(Transaction.Status.ERROR);
          }
          transactionRepository.save(initialTransaction);
        });
  }

  private long waitForReversalTransaction(Transaction chargeTransaction) {
    return System.currentTimeMillis() - chargeTransaction.getTimestamp();
  }

  private boolean authorizeTransaction(Payment payment, Transaction transaction) {
    boolean isAuthorized = false;
    if (payment.getAmount() > payment.getCustomer().getCustomerAmount()) {
      transaction.setStatus(Transaction.Status.ERROR);
    } else {
      transaction.setStatus(Transaction.Status.APPROVED);
      isAuthorized = true;
    }
    transactionRepository.save(transaction);
    return isAuthorized;
  }

  @Override
  public List<Transaction> getTransactionsForMerchant(Principal merchant) {
    return transactionRepository.findAllByMerchantId(merchant.getId());
  }

  @Override
  public List<Transaction> getAllTransactionsGroupByMerchant() {
    return transactionRepository.findAllTransactionsGroupByMerchant();
  }

  @Override
  public Optional<Transaction> getTransactionInformation(String uuid) {
    return transactionRepository.findById(uuid);
  }

  @Override
  public boolean isPaymentTypeAllowed(Payment payment) {
    if (payment.getTransactionType() != TransactionType.CHARGE
        || payment.getTransactionType() != TransactionType.REFUND
        || payment.getTransactionType() != TransactionType.REVERSAL) {
      return false;
    } else {
      if (payment.getTransactionType() == TransactionType.REFUND
          || payment.getTransactionType() == TransactionType.REVERSAL) {
        return payment.getReferenceId() != null;
      }
    }
    return true;
  }
}
