package com.emerchantpay.test.paymentsystembackend.controllers.v1;

import com.emerchantpay.test.paymentsystembackend.model.Principal;
import com.emerchantpay.test.paymentsystembackend.services.impl.PrincipalService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/admin")
public class AdminController {

  @Autowired private PrincipalService principalService;

  @GetMapping("/merchants")
  @PreAuthorize("@authorizationService.authorizeAdmin()")
  public ResponseEntity<?> getMerchants(Authentication authentication) {
    List<Principal> list = principalService.findAllMerchants();
    return ResponseEntity.ok(list);
  }

  @GetMapping("/merchants/{merchantId}")
  @PreAuthorize(
      "@authorizationService.authorizeAdmin() ||"
          + " (@authorizationService.authorizeMerchant() && @authorizationService.matchAuthenticatedPrincipalWithId(#merchantId)) ")
  public ResponseEntity<?> getMerchant(@PathVariable("merchantId") String merchantId) {
    Optional<Principal> principal = principalService.findPrincipalById(Long.parseLong(merchantId));
    if (principal.isPresent()) {
      return ResponseEntity.ok(principal);
    }
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(String.format("Merchant with id %s is not found!", merchantId));
  }

  @DeleteMapping("/merchants/{merchantId}")
  @PreAuthorize("@authorizationService.authorizeAdmin()")
  public ResponseEntity<?> deleteMerchant(@PathVariable("merchantId") String merchantId) {
    long merchId = Long.parseLong(merchantId);
    if (principalService.findPrincipalById(merchId).isPresent()) {
      principalService.deleteMerchantById(merchId);
    } else {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(String.format("Merchant with id %s is not found!", merchantId));
    }
    return ResponseEntity.ok(String.format("Delete merchant with id", merchId));
  }

  @PutMapping("/merchants/{merchantId}/status/{status}")
  @PreAuthorize("@authorizationService.authorizeAdmin()")
  public ResponseEntity<?> changeMerchantStatus(
      @PathVariable String merchantId, @PathVariable String status, Authentication authentication) {
    Principal.Status newStatus = Enum.valueOf(Principal.Status.class, status.toUpperCase());
    Optional<Principal> merchantOptional =
        principalService.findPrincipalById(Long.parseLong(merchantId));
    if (merchantOptional.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(String.format("Merchant with id %s is not found!", merchantId));
    }
    Principal merchant = merchantOptional.get();
    merchant.setStatus(newStatus);
    Principal updatedPrincipal = principalService.createOrUpdatePrincipal(merchant);
    return ResponseEntity.ok().body(updatedPrincipal);
  }
}
