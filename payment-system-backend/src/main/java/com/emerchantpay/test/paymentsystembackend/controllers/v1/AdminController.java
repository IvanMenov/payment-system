package com.emerchantpay.test.paymentsystembackend.controllers.v1;

import com.emerchantpay.test.paymentsystembackend.model.Principal;
import com.emerchantpay.test.paymentsystembackend.model.PrincipalType;
import com.emerchantpay.test.paymentsystembackend.model.Transaction;
import com.emerchantpay.test.paymentsystembackend.services.IPaymentService;
import com.emerchantpay.test.paymentsystembackend.services.IPrincipalService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/admin")
public class AdminController {

  @Autowired private IPrincipalService principalService;

  @Autowired private IPaymentService paymentService;

  @PutMapping("/{merchantId}/update/{status}")
  public ResponseEntity<?> getTransactionForMerchant(
      @PathVariable String merchantId, @PathVariable String status, Authentication authentication) {
    if (checkIsPrincipalAdmin(authentication)) {
      return new ResponseEntity<String>(
          "Merchants not allowed to update status!", HttpStatus.FORBIDDEN);
    }
    long merchId = 0;
    try {
      merchId = Long.parseLong(merchantId);
    } catch (NumberFormatException ex) {
      return ResponseEntity.badRequest().body(ex.getMessage());
    }
    Principal.Status newStatus = null;
    try {
      newStatus = Enum.valueOf(Principal.Status.class, status.toUpperCase());
    } catch (Exception ex) {
      return ResponseEntity.badRequest().body(ex.getMessage());
    }
    Optional<Principal> merchantOptional = principalService.findPrincipalById(merchId);
    if (!merchantOptional.isPresent()) {
      return ResponseEntity.notFound().build();
    }
    Principal merchant = merchantOptional.get();
    if (merchant.getPrincipalType() == PrincipalType.MERCHANT) {
      return new ResponseEntity<String>(
          "Merchants not allowed to update status!", HttpStatus.FORBIDDEN);
    }
    merchant.setStatus(newStatus);
    principalService.createOrUpdatePrincipal(merchant);
    return ResponseEntity.ok().body(String.format("Successfully updated merchant %s", merchantId));
  }

  @GetMapping("/transactions/all")
  public ResponseEntity<?> getAllTransactionGroupByMerchant(Authentication authentication) {
    if (checkIsPrincipalAdmin(authentication)) {
      return new ResponseEntity<String>(
          "Merchants not allowed to update status!", HttpStatus.FORBIDDEN);
    }
    List<Transaction> transactions = paymentService.getAllTransactionsGroupByMerchant();
    return ResponseEntity.ok(transactions);
  }

  private boolean checkIsPrincipalAdmin(Authentication authentication) {
    Principal principal = (Principal) authentication.getPrincipal();
    return principal.getPrincipalType() != PrincipalType.MERCHANT;
  }
}
