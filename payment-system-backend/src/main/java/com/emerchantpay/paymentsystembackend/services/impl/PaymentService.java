package com.emerchantpay.paymentsystembackend.services.impl;

import com.emerchantpay.paymentsystembackend.repositories.PrincipalRepository;
import com.emerchantpay.paymentsystembackend.repositories.TransactionRepository;
import com.emerchantpay.paymentsystembackend.services.IPaymentService;
import com.emerchantpay.paymentsystembackend.utils.TransactionFactory;
import com.emerchantpay.paymentsystembackend.model.PaymentDTO;
import com.emerchantpay.paymentsystembackend.model.Principal;
import com.emerchantpay.paymentsystembackend.model.Transaction;
import com.emerchantpay.paymentsystembackend.model.TransactionType;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.OptimisticLockException;
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

  @Autowired private PrincipalRepository principalRepository;

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
          transactionRepository.save(initializedTransaction);
          principalRepository.save(merchant);
          return initializedTransaction;
        });
  }

  /**
   * @param merchant
   * @param payment Asynchronously start the validation process. If the initialTransaction is a
   *     REVERSAL check:
   *                1. Finds a transaction provided in the referenceId by the payment.
   *                2. If check 1 succeeds than checks if the transaction found is of TransactionType.AUTHORIZE.
   *                3. If check 2 succeeds, check if the transaction found has status Transaction.Status.APPROVED.
   *                4. If check 3 succeeds than checks if time window between the authorization transaction
   *                creation and now is less than specific WAIT_TIME (defaults to 40 sec, but can be configured
   *                through app.wait.time.seconds in application-dev.properties). If all checks pass, set status
   *                of authorization transaction to REVERSED, change status of reversal transaction to
   *                approved and if there is a charge transaction that is still not finished, set it to status
   *                ERROR. If any of the above checks fail this would set initialTransaction status to ERROR.
   *
   *     If the initialTransaction for REFUND check:
   *                1. Finds a transaction provided in the referenceId by the payment.
   *                2. If check 1 succeeds check the found transaction amount is greater than or equal
   *                to payment amount.
   *                3. If check 2 succeeds than checks if the transaction found is of TransactionType.CHARGE.
   *                4. If check 3 succeeds check if the transaction found has status Transaction.Status.APPROVED.
   *                If the above checks would succeed, then update the merchant amount by subtracting the
   *                charge transaction amount from the current merchant's total transaction, set refund
   *                transaction to approved and set charge transaction to refunded. If any of the above checks
   *                fail this would set initialTransaction status to ERROR.
   *
   *     If the initialTransaction for CHARGE check:
   *                1.Authorize the initialTransaction and set it to by checking that
   *                payment amount < customer's amount.
   *                2.If check 1 succeeds create charge transaction and set it to refer to the initialTransaction.
   *                Then checks if timewindow between the authorization transaction creation and now is less than
   *                specific WAIT_TIME (defaults to 40 sec, but can be configured through app.wait.time.seconds
   *                in application-dev.properties). If it is true, call TimeUnit.MILLISECONDS.sleep for the remaining time.
   *                Then check if in the meantime there was a reversal transaction which would change the status
   *                of initialTransaction transaction from Approved to Reversed
   *                3.If check 2 succeeds then add the change status of chargeTransaction
   *                to Transaction.Status.APPROVED and update merchant total transaction sum.
   *
   *     If any of the above checks fail this would set initialTransaction status to ERROR.
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
      initialTransaction.setTransactionStatus(Transaction.Status.ERROR);
    } else {
      initialTransaction.setTransactionStatus(Transaction.Status.APPROVED);
      isAuthorizationSuccessful = true;
    }
    transactionRepository.save(initialTransaction);
    return isAuthorizationSuccessful;
  }

  private void performReversal(Transaction initialTransaction) {
    // find the authorization transaction first;
    transactionTemplate.executeWithoutResult(
        (transactionStatus) -> {
          Transaction authTransaction = null;
          Optional<Transaction> authorizationTransaction =
              transactionRepository.findById(initialTransaction.getReferenceTransactionUUID());
          if (authorizationTransaction.isPresent()
              && (authorizationTransaction.get().getTransactionType() == TransactionType.AUTHORIZE)
              && (authorizationTransaction.get().getTransactionStatus() == Transaction.Status.APPROVED)
              && waitForReversalTransaction(authorizationTransaction.get()) < WAIT_TIME) {
            authTransaction = authorizationTransaction.get();

            authTransaction.setTransactionStatus(Transaction.Status.REVERSED);
            initialTransaction.setTransactionStatus(Transaction.Status.APPROVED);
            authTransaction.setReferenceTransactionUUID(initialTransaction.getUuid());

          } else {
            initialTransaction.setTransactionStatus(Transaction.Status.ERROR);
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
            if (transactionRepository.findById(initialTransaction.getUuid()).get().getTransactionStatus()
                == Transaction.Status.APPROVED) {
              chargeTransaction.setTransactionStatus(Transaction.Status.APPROVED);
              // set reference of the authorization transaction to charge transaction
              initialTransaction.setReferenceTransactionUUID(chargeTransaction.getUuid());
              transactionRepository.save(initialTransaction);

              boolean isUpdated = false;
              Principal principal = principalRepository.findPrincipalById(merchant.getId()).get();

              // in case the scheduledCleanup deleted a CHARGE transaction
              // it would decrease the total transaction sum for the merchant and update the version
              // property for that merchant
              // which would trigger OptimisticLockException exception
              // In that case we can retry getting the principal calling findPrincipalById retry
              // updating the principal
              while (!isUpdated) {
                try {
                  principal.updateTotalTransactionSum(payment.getAmount(), true);
                  principalRepository.save(principal);
                  isUpdated = true;
                } catch (OptimisticLockException exception) {
                  principal = principalRepository.findPrincipalById(merchant.getId()).get();
                }
              }

            } else {
              chargeTransaction.setTransactionStatus(Transaction.Status.ERROR);
            }
            chargeTransaction.setTimestamp(System.currentTimeMillis());
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
              && (chargeTransactionOptional.get().getTransactionType() == TransactionType.CHARGE)
              && (chargeTransactionOptional.get().getTransactionStatus() == Transaction.Status.APPROVED)) {
            chargeTransaction = chargeTransactionOptional.get();
            initialTransaction.setTransactionStatus(Transaction.Status.APPROVED);
            chargeTransaction.setTransactionStatus(Transaction.Status.REFUNDED);
            transactionRepository.save(chargeTransaction);

            boolean isUpdated = false;
            Principal principal = principalRepository.findPrincipalById(merchant.getId()).get();
            while (!isUpdated) {
              try {
                principal.updateTotalTransactionSum(payment.getAmount(), false);
                principalRepository.save(principal);
                isUpdated = true;
              } catch (OptimisticLockException exception) {
                principal = principalRepository.findPrincipalById(merchant.getId()).get();
              }
            }
          } else {
            initialTransaction.setTransactionStatus(Transaction.Status.ERROR);
          }

          transactionRepository.save(initialTransaction);
        });
  }

  private long waitForReversalTransaction(Transaction chargeTransaction) {
    return System.currentTimeMillis() - chargeTransaction.getTimestamp();
  }



  /**
   * @param uuid get information for a specific transaction
   * @return
   */
  @Override
  public Optional<Transaction> getTransactionInformation(String uuid) {
    return transactionRepository.findById(uuid);
  }

  /**
   * @param payment if payment is of type REFUND or REVERSAL, they should provide as a referenceId
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
