package com.emerchantpay.test.paymentsystembackend.services.impl;

import com.emerchantpay.test.paymentsystembackend.model.Principal;
import com.emerchantpay.test.paymentsystembackend.repositories.PrincipalRepository;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class PrincipalUserDetailsService implements UserDetailsService {
  @Autowired private PrincipalRepository principalRepository;

  /**
   * @param email
   *     <p>Loading the principal by username. Used in AuthTokenFilter to update the SecurityContext
   *     with the principal.
   * @return UserDetails
   * @throws UsernameNotFoundException
   */
  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    Optional<Principal> optionalPrincipal = principalRepository.findByEmail(email);
    if (optionalPrincipal.isPresent()) {
      return optionalPrincipal.get();
    }
    throw new UsernameNotFoundException(String.format("%s not found"));
  }
}
