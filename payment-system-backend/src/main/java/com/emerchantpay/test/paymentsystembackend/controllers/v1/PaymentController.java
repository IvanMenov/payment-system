package com.emerchantpay.test.paymentsystembackend.controllers.v1;

import com.emerchantpay.test.paymentsystembackend.exceptions.MerchantNotActivatedException;
import com.emerchantpay.test.paymentsystembackend.exceptions.PaymentTypeNotAllowedException;
import com.emerchantpay.test.paymentsystembackend.exceptions.TranctionAlreadySubmittedException;
import com.emerchantpay.test.paymentsystembackend.model.Payment;
import com.emerchantpay.test.paymentsystembackend.model.Principal;
import com.emerchantpay.test.paymentsystembackend.model.Transaction;
import com.emerchantpay.test.paymentsystembackend.services.IPaymentService;
import com.emerchantpay.test.paymentsystembackend.services.IPrincipalService;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/payment/transactions")
public class PaymentController {

  @Autowired private IPaymentService paymentService;

  @Autowired private IPrincipalService principalService;

  @PostMapping("/init")
  public ResponseEntity<?> initializeTransaction(
      @RequestBody Payment payment, Authentication authentication) {
    Principal merchant = (Principal) authentication.getPrincipal();
    if (principalService.isMerchantInactive(merchant)) {
      throw new MerchantNotActivatedException(merchant.getEmail());
    }
    if (paymentService.isTransactionAlreadySubmitted(payment)) {
      throw new TranctionAlreadySubmittedException(payment.getUuid());
    }
    if (paymentService.isPaymentTypeAllowed(payment)) {
      throw new PaymentTypeNotAllowedException(payment.getTransactionType().getType());
    }

    Transaction transaction = paymentService.initializeTransaction(merchant, payment);
    paymentService.commenceTransactionValidations(merchant, payment, transaction);
    return ResponseEntity.created(URI.create(String.format("/v1/payment/transactions/%s/monitor")))
        .build();
  }

  @GetMapping("/{transactionId}/monitor")
  public ResponseEntity<?> monitorTransaction(@PathVariable("transactionId") String transactionId) {
    Optional<Transaction> transactionOptional =
        paymentService.getTransactionInformation(transactionId);
    if (transactionOptional.isPresent()) {
      return ResponseEntity.ok(transactionOptional.get());
    } else {
      return ResponseEntity.badRequest()
          .body(String.format("Transaction id: %s not found!", transactionId));
    }
  }

  @GetMapping
  public ResponseEntity<?> getTransactionForMerchant(Authentication authentication) {
    Principal merchant = (Principal) authentication.getPrincipal();
    List<Transaction> transactions = paymentService.getTransactionsForMerchant(merchant);
    return ResponseEntity.ok(transactions);
  }
}
