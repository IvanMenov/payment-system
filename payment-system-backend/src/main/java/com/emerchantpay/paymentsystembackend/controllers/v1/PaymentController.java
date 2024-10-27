package com.emerchantpay.paymentsystembackend.controllers.v1;

import com.emerchantpay.paymentsystembackend.exceptions.MerchantNotActivatedException;
import com.emerchantpay.paymentsystembackend.exceptions.NoReferenceIdException;
import com.emerchantpay.paymentsystembackend.exceptions.TranctionAlreadySubmittedException;
import com.emerchantpay.paymentsystembackend.model.KafkaMessage;
import com.emerchantpay.paymentsystembackend.model.PaymentDTO;
import com.emerchantpay.paymentsystembackend.model.Principal;
import com.emerchantpay.paymentsystembackend.model.Transaction;
import com.emerchantpay.paymentsystembackend.services.IMessageProducerService;
import com.emerchantpay.paymentsystembackend.services.IPaymentService;
import com.emerchantpay.paymentsystembackend.services.IPrincipalService;
import com.emerchantpay.paymentsystembackend.validation.Payment;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/payment/transactions")
public class PaymentController {

  @Autowired private IPaymentService paymentService;

  @Autowired private IPrincipalService<Principal> principalService;

  @Autowired private IMessageProducerService messageProducerService;

  @PostMapping(
      value = "/init/{merchantId}",
      consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  public ResponseEntity<?> initializeTransaction(
      @RequestBody @Payment PaymentDTO payment, @PathVariable("merchantId") String merchantId, Authentication authentication) {
    Optional<Principal> optionalPrincipal = principalService.findPrincipalById(Long.parseLong(merchantId));
    if(optionalPrincipal.isEmpty()){
      throw new RuntimeException("No merchant with id: "+ merchantId);
    }
    Principal merchant = optionalPrincipal.get();
    JwtAuthenticationToken jwtAuthenticationToken = (JwtAuthenticationToken) authentication;
    if(!Objects.equals(jwtAuthenticationToken.getName(), merchant.getEmail())){
      throw new RuntimeException(
              String.format("Merchant with email %s doesn't match with authenticated principal's email: %s",
              merchant.getEmail(), jwtAuthenticationToken.getName()));
    }

    if (principalService.isMerchantInactive(merchant)) {
      throw new MerchantNotActivatedException(merchant.getEmail());
    }
    if (paymentService.isTransactionAlreadySubmitted(payment)) {
      throw new TranctionAlreadySubmittedException(payment.getUuid());
    }
    if (paymentService.hasNoReferenceId(payment)) {
      throw new NoReferenceIdException(payment.getTransactionType().getType());
    }
    if (payment.getUuid() == null) {
      payment.setUuid(UUID.randomUUID().toString());
    }

    KafkaMessage kafkaMessage = new KafkaMessage(merchant, payment);
    messageProducerService.sendMessage(kafkaMessage);
    return ResponseEntity.created(
            URI.create(String.format("api/v1/payment/transactions/%s/monitor", payment.getUuid())))
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

/*  @GetMapping
  public ResponseEntity<?> getTransactionForMerchant(Authentication authentication) {
    //Principal merchant = (Principal) authentication.getPrincipal();
    JwtAuthenticationToken jwtAuthenticationToken = (JwtAuthenticationToken) authentication;
    List<Transaction> transactions = paymentService.getTransactionsForMerchant(merchant);
    return ResponseEntity.ok(transactions);
  }*/
}
