package com.emerchantpay.test.paymentsystembackend.services.impl;

import com.emerchantpay.test.paymentsystembackend.model.Principal;
import com.emerchantpay.test.paymentsystembackend.model.PrincipalType;
import com.emerchantpay.test.paymentsystembackend.services.IAuthorizationService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service("authorizationService")
public class AuthorizationService implements IAuthorizationService {

  @Override
  public boolean authorizeAdmin() {
    Principal principal =
        (Principal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    return principal.getPrincipalType() == PrincipalType.ADMIN;
  }

  @Override
  public boolean authorizeMerchant() {
    Principal principal =
        (Principal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    return principal.getPrincipalType() == PrincipalType.MERCHANT;
  }

  @Override
  public boolean matchAuthenticatedPrincipalWithId(String id) {
    Principal principal =
        (Principal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    return principal.getId() == Long.parseLong(id);
  }
}
