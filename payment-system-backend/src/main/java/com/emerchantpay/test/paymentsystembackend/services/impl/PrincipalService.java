package com.emerchantpay.test.paymentsystembackend.services.impl;

import com.emerchantpay.test.paymentsystembackend.model.Principal;
import com.emerchantpay.test.paymentsystembackend.model.PrincipalType;
import com.emerchantpay.test.paymentsystembackend.repositories.PrincipalRepository;
import com.emerchantpay.test.paymentsystembackend.services.IImportPrincipalService;
import com.emerchantpay.test.paymentsystembackend.services.IPrincipalService;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PrincipalService
    implements UserDetailsService, IPrincipalService, IImportPrincipalService {

  private static final String ADMIN = "admin";
  @Autowired private PrincipalRepository principalRepository;

  @Autowired private PasswordEncoder passwordEncoder;

  @Override
  public void createOrUpdatePrincipal(Principal merchant) {
    principalRepository.save(merchant);
  }

  @Override
  public List<Principal> findAllAdmins() {
    return principalRepository.findAllByPrincipalType(PrincipalType.ADMIN.getType());
  }

  @Override
  public List<Principal> findAllMerchants() {
    return principalRepository.findAllByPrincipalType(PrincipalType.MERCHANT.getType());
  }

  @Override
  public boolean isMerchantInactive(Principal merchant) {
    Optional<Principal> optionalPrincipal =
        principalRepository.findByIdAndPrincipalType(
            merchant.getId(), PrincipalType.MERCHANT.getType());
    if (optionalPrincipal.isPresent()) {
      return optionalPrincipal
          .get()
          .getStatus()
          .getStatusValue()
          .equals(Principal.Status.INACTIVE.getStatusValue());
    }
    return false;
  }

  @Override
  public Optional<Principal> findPrincipalById(long id) {
    return principalRepository.findById(id);
  }

  @Override
  public Optional<Principal> findPrincipalByEmail(String email) {
    return principalRepository.findByEmail(email);
  }

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    Optional<Principal> optionalPrincipal = principalRepository.findByEmail(email);
    if (optionalPrincipal.isPresent()) {
      return optionalPrincipal.get();
    }
    throw new UsernameNotFoundException(String.format("%s not found"));
  }

  @Override
  public void importPrincipalsFromCsv(MultipartFile file) {
    CSVParser parser = new CSVParserBuilder().withSeparator(',').withIgnoreQuotations(true).build();

    try (Reader reader = new InputStreamReader(file.getInputStream());
        CSVReader csvReader =
            new CSVReaderBuilder(reader).withSkipLines(0).withCSVParser(parser).build(); ) {
      csvReader
          .readAll()
          .forEach(
              array -> {
                Principal principal = null;
                if (array[0].equalsIgnoreCase(PrincipalType.ADMIN.getType())) {
                  principal =
                      Principal.builder()
                          .email(array[3])
                          .password(passwordEncoder.encode(array[4]))
                          .principalType(PrincipalType.ADMIN)
                          .status(Principal.Status.ACTIVE)
                          .build();

                } else if (array[0].equalsIgnoreCase(PrincipalType.MERCHANT.getType())) {
                  principal =
                      Principal.builder()
                          .name(array[1])
                          .description(array[2])
                          .email(array[3])
                          .password(passwordEncoder.encode(array[4]))
                          .principalType(PrincipalType.MERCHANT)
                          .status(Principal.Status.INACTIVE)
                          .build();
                }
                if (principal != null) {
                  createOrUpdatePrincipal(principal);
                }
              });

    } catch (Exception exception) {
      throw new RuntimeException(exception.getMessage());
    }
  }
}
