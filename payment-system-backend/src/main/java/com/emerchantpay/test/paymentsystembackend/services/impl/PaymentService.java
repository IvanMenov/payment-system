package com.emerchantpay.test.paymentsystembackend.services.impl;

import com.emerchantpay.test.paymentsystembackend.model.PaymentDTO;
import com.emerchantpay.test.paymentsystembackend.model.Principal;
import com.emerchantpay.test.paymentsystembackend.model.Transaction;
import com.emerchantpay.test.paymentsystembackend.model.TransactionType;
import com.emerchantpay.test.paymentsystembackend.repositories.MerchantRepository;
import com.emerchantpay.test.paymentsystembackend.repositories.TransactionRepository;
import com.emerchantpay.test.paymentsystembackend.services.IPaymentService;
import com.emerchantpay.test.paymentsystembackend.utils.TransactionFactory;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class PaymentService implements IPaymentService {

  @Value("${app.wait.time.seconds:60}")
  private long waitTimeInSeconds;

  private static long WAIT_TIME;

  @Autowired private TransactionRepository transactionRepository;

  @Autowired private MerchantRepository merchantRepository;

  @Autowired private TransactionTemplate transactionTemplate;

  @PostConstruct
  private void setUp() {
    WAIT_TIME = waitTimeInSeconds * 1000;
  }

  /**
   * @param payment if payment objects provided uuid and there is already an existing transaction
   *     with that id the check would return true otherwise it would return false
   * @return
   */
  @Override
  public boolean isTransactionAlreadySubmitted(PaymentDTO payment) {
    if (payment.getUuid() == null) {
      return false;
    }
    return transactionRepository.findById(payment.getUuid()).isPresent();
  }

  /**
   * @param merchant
   * @param payment
   *     <p>creates an initial Transaction based on PaymentDTO provided valid options for payment
   *     transactions types are:
   *     <p>TransactionType.REFUND , TransactionType.CHARGE, TransactionType.REVERSAL
   *     <p>Valid options are: CHARGE, REFUND, REVERSAL
   * @return Transaction
   * @throws IllegalArgumentException when AUTHORIZE is provided the method for transactionType
   */
  @Override
  public Transaction initializeTransaction(Principal merchant, PaymentDTO payment) {
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

  /**
   * @param merchant
   * @param payment
   *     <p>Asynchronously start the validation process.
   *     If the initialTransaction for REVERSAL check:
   *         1. finds a transaction provided in the referenceId by the payment
   *         2. If check 1 succeeds than checks if the transaction found is of TransactionType.AUTHORIZE
   *         3. if check 2 succeeds check if the transaction found has status Transaction.Status.APPROVED
   *         4. if check 3 succeeds than checks if time window between the
   *            authorization transaction creation and now is less than specific WAIT_TIME (defaults to 40
   *            sec, but can be configured through app.wait.time.seconds in application.properties)
   *            If all checks pass, set status of authorization transaction to REVERSED, change status of reversal transaction to approved,
   *            and if there is a charge transaction that is still not finished, set it to status ERROR.
   *            If any of the above checks fail this would set initialTransaction status to ERROR.
   *
   *
   *     If the initialTransaction for REFUND check:
   *        1. finds a transaction provided in the referenceId by the payment
   *        2. if check 1 succeeds check the found transaction amount is greater than or equal to payment amoount
   *        3. if check 2 succeeds than checks if the transaction found is of TransactionType.CHARGE
   *        4. if check 3 succeeds check if the transaction found has status Transaction.Status.APPROVED
   *
   *        If the above checks would succeed, then update the merchant amount by subtracting the charge transaction amount from the current merchant's
   *        total transaction, set refund transaction to approved and set charge transaction to refunded
   *
   *        If any of the above checks fail this would set initialTransaction status to ERROR
   *
   *
   *      If the initialTransaction for CHARGE check:
   *      
   *        1. Authorize the initialTransaction and set it to by checking that payment amount < customer's amount.
   *        2. If check 1 succeeds create charge transaction and set it to refer to the initialTransaction
   *            Then checks if timewindow between the authorization transaction creation
   *            and now is less than specific WAIT_TIME (defaults to 40 sec, but can be configured through
   *            app.wait.time.seconds in application.properties). If it is true, call TimeUnit.MILLISECONDS.sleep for the remaining time.
   *            Then check if in the meantime there was a reversal transaction which would change the status
   *            of initialTransaction transaction from Approved to Reversed
   *        3. if check 2 succeeds then add the change status of chargeTransaction to Transaction.Status.APPROVED
   *            and update merchant total transaction sum
   *
   *        If any of the above checks fail this would set initialTransaction status to ERROR
   * @param initialTransaction
   */
  @Override
  @Async
  public void commenceTransactionProcess(
      Principal merchant, PaymentDTO payment, Transaction initialTransaction) {
    if (payment.getTransactionType() == TransactionType.REVERSAL) {
      performReversal(initialTransaction);
    } else if (payment.getTransactionType() == TransactionType.REFUND) {
      performRefunding(merchant, payment, initialTransaction);
    } else if (payment.getTransactionType() == TransactionType.CHARGE) {
      preformCharging(merchant, payment, initialTransaction);
    }
  }

  private boolean isAuthorizationSuccessful(
      Principal merchant, PaymentDTO payment, Transaction initialTransaction) {
    boolean isAuthorizationSuccessful = false;
    if (payment.getAmount() > payment.getCustomer().getCustomerAmount()) {
      initialTransaction.setStatus(Transaction.Status.ERROR);
    } else {
      initialTransaction.setStatus(Transaction.Status.APPROVED);
      isAuthorizationSuccessful = true;
    }
    transactionRepository.save(initialTransaction);
    return isAuthorizationSuccessful;
  }

  void performReversal(Transaction initialTransaction) {
    // find the authorization transaction first;
    transactionTemplate.executeWithoutResult(
        (transactionStatus) -> {
          Transaction authTransaction = null;
          Optional<Transaction> authorizationTransaction =
              transactionRepository.findById(initialTransaction.getReferenceTransactionUUID());
          if (authorizationTransaction.isPresent()
              && (authorizationTransaction.get().getType() == TransactionType.AUTHORIZE)
              && (authorizationTransaction.get().getStatus() == Transaction.Status.APPROVED)
              && waitForReversalTransaction(authorizationTransaction.get()) < WAIT_TIME) {
            authTransaction = authorizationTransaction.get();

            authTransaction.setStatus(Transaction.Status.REVERSED);
            initialTransaction.setStatus(Transaction.Status.APPROVED);
            authTransaction.setReferenceTransactionUUID(initialTransaction.getUuid());

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

  private void preformCharging(
      Principal merchant, PaymentDTO payment, Transaction initialTransaction) {
    if (isAuthorizationSuccessful(merchant, payment, initialTransaction)) {
      transactionTemplate.executeWithoutResult(
          (transactionStatus) -> {
            // initialTransaction is of type AUTHORIZE
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
          });
    }
  }

  private void performRefunding(
      Principal merchant, PaymentDTO payment, Transaction initialTransaction) {
    transactionTemplate.executeWithoutResult(
        (transactionStatus) -> {
          // find charge transaction
          Transaction chargeTransaction = null;
          Optional<Transaction> chargeTransactionOptional =
              transactionRepository.findById(initialTransaction.getReferenceTransactionUUID());
          if (chargeTransactionOptional.isPresent()
              && (payment.getAmount() <= chargeTransactionOptional.get().getAmount())
              && (chargeTransactionOptional.get().getType() == TransactionType.CHARGE)
              && (chargeTransactionOptional.get().getStatus() == Transaction.Status.APPROVED)) {
            chargeTransaction = chargeTransactionOptional.get();
            boolean shouldRetry;
            do {
              shouldRetry = merchant.updateTotalTransactionSum(payment.getAmount(), false);
            } while (!shouldRetry);
            initialTransaction.setStatus(Transaction.Status.APPROVED);
            chargeTransaction.setStatus(Transaction.Status.REFUNDED);
            transactionRepository.save(chargeTransaction);
            merchantRepository.updateTotalTransactionSum(
                merchant.getId(), merchant.getTotalTransactionSum());

          } else {
            initialTransaction.setStatus(Transaction.Status.ERROR);
          }

          transactionRepository.save(initialTransaction);
        });
  }

  private long waitForReversalTransaction(Transaction chargeTransaction) {
    return System.currentTimeMillis() - chargeTransaction.getTimestamp();
  }

    /**
     * @param merchant
     * get all transactions for a given merchant
     * @return
     */
  @Override
  public List<Transaction> getTransactionsForMerchant(Principal merchant) {
    return transactionRepository.findAllByMerchantId(merchant.getId());
  }

    /**
     * @param uuid
     * get information for a specific transaction
     * @return
     */
  @Override
  public Optional<Transaction> getTransactionInformation(String uuid) {
    return transactionRepository.findById(uuid);
  }

    /**
     * @param payment
     * if payment is of type REFUND or REVERSAL, they should provide as a referenceId
     * @return
     */
  @Override
  public boolean hasNoReferenceId(PaymentDTO payment) {
    if (payment.getTransactionType() == TransactionType.REFUND
        || payment.getTransactionType() == TransactionType.REVERSAL) {
      return payment.getReferenceId() == null;
    }
    return false;
  }
}
