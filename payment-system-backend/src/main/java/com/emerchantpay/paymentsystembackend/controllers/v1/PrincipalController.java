package com.emerchantpay.paymentsystembackend.controllers.v1;

import com.emerchantpay.paymentsystembackend.model.Principal;
import com.emerchantpay.paymentsystembackend.model.PrincipalType;
import com.emerchantpay.paymentsystembackend.services.impl.PrincipalService;
import com.emerchantpay.paymentsystembackend.utils.Constants;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class PrincipalController {
  @Autowired private PrincipalService principalService;

  @GetMapping(value = "/principal/signin")
  public Principal signin(Authentication authentication) {
    JwtAuthenticationToken jwtAuthenticationToken = (JwtAuthenticationToken) authentication;
    Principal principal = null;
    Optional<Principal> optionalPrincipal =
        principalService.findPrincipalByEmail(jwtAuthenticationToken.getName());
    if (optionalPrincipal.isEmpty()) {
      Set<String> set =
          jwtAuthenticationToken.getAuthorities().stream()
              .map(GrantedAuthority::getAuthority)
              .filter(
                  authority ->
                      authority.contains("ROLE_" + Constants.PAYMENT_SYSTEM_ADMIN_ROLE)
                          || authority.contains("ROLE_" + Constants.PAYMENT_SYSTEM_MERCHANT_ROLE))
              .collect(Collectors.toSet());
      if (set.contains("ROLE_" + Constants.PAYMENT_SYSTEM_ADMIN_ROLE)) {
        principal =
            Principal.builder()
                .email(jwtAuthenticationToken.getName())
                .principalType(PrincipalType.ADMIN)
                .principalStatus(Principal.Status.ACTIVE)
                .build();
      } else if (set.contains("ROLE_" + Constants.PAYMENT_SYSTEM_MERCHANT_ROLE)) {
        principal =
            Principal.builder()
                .email(jwtAuthenticationToken.getName())
                .principalType(PrincipalType.MERCHANT)
                .principalStatus(Principal.Status.INACTIVE)
                .totalTransactionSum(BigDecimal.valueOf(0))
                .build();
      }
      return principalService.createOrUpdatePrincipal(principal);
    }
    return optionalPrincipal.get();
  }

  @GetMapping("/admin/merchants")
  public ResponseEntity<?> getMerchants() {
    List<Principal> list = principalService.findAllMerchants();
    return ResponseEntity.ok(list);
  }

  @GetMapping("/merchants/{merchantId}")
  // TODO: @authorizationService.matchAuthenticatedPrincipalWithId(#merchantId))
  public ResponseEntity<?> getMerchant(@PathVariable("merchantId") String merchantId,
                                       @RequestParam("limit")int limit, @RequestParam("offset")int offset ) {
    Optional<Principal> principal =
            principalService.findPrincipalByIdLoadBatchOfTransactions(Long.parseLong(merchantId), limit, offset);
    if (principal.isPresent()) {
      return ResponseEntity.ok(principal);
    }
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(String.format("Merchant with id %s is not found!", merchantId));
  }

  @DeleteMapping("/admin/merchants/{merchantId}")
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

  @PutMapping("/admin/merchants/{merchantId}/status/{status}")
  public ResponseEntity<?> changeMerchantStatus(
          @PathVariable String merchantId, @PathVariable String status) {
    Principal.Status newStatus = Enum.valueOf(Principal.Status.class, status.toUpperCase());
    Optional<Principal> merchantOptional =
            principalService.findPrincipalById(Long.parseLong(merchantId));
    if (merchantOptional.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
              .body(String.format("Merchant with id %s is not found!", merchantId));
    }
    Principal merchant = merchantOptional.get();
    merchant.setPrincipalStatus(newStatus);
    Principal updatedPrincipal = principalService.createOrUpdatePrincipal(merchant);
    return ResponseEntity.ok().body(updatedPrincipal);
  }
}
