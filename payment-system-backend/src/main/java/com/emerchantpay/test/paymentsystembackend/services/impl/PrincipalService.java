package com.emerchantpay.test.paymentsystembackend.services.impl;

import com.emerchantpay.test.paymentsystembackend.model.Principal;
import com.emerchantpay.test.paymentsystembackend.model.PrincipalType;
import com.emerchantpay.test.paymentsystembackend.repositories.PrincipalRepository;
import com.emerchantpay.test.paymentsystembackend.services.IAuthenticationService;
import com.emerchantpay.test.paymentsystembackend.services.IImportPrincipalService;
import com.emerchantpay.test.paymentsystembackend.services.IPrincipalService;
import com.google.common.util.concurrent.AtomicDouble;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PrincipalService implements IPrincipalService, IImportPrincipalService {

  @Autowired private PrincipalRepository principalRepository;

  @Autowired private IAuthenticationService authenticationService;

  @Override
  public void createOrUpdatePrincipal(Principal merchant) {
    principalRepository.save(merchant);
  }

  @Override
  public List<Principal> findAllAdmins() {
    return principalRepository.findAllByPrincipalType(PrincipalType.ADMIN.ordinal());
  }

  @Override
  public List<Principal> findAllMerchants() {
    return principalRepository.findAllByPrincipalType(PrincipalType.MERCHANT.ordinal());
  }

  @Override
  public boolean isMerchantInactive(Principal merchant) {
    Optional<Principal> optionalPrincipal =
        principalRepository.findByIdAndPrincipalType(
            merchant.getId(), PrincipalType.MERCHANT.ordinal());
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
                          .password(authenticationService.encodePassword(array[4]))
                          .principalType(PrincipalType.ADMIN)
                          .status(Principal.Status.ACTIVE)
                          .build();

                } else if (array[0].equalsIgnoreCase(PrincipalType.MERCHANT.getType())) {
                  principal =
                      Principal.builder()
                          .name(array[1])
                          .description(array[2])
                          .email(array[3])
                          .password(authenticationService.encodePassword(array[4]))
                          .principalType(PrincipalType.MERCHANT)
                          .status(Principal.Status.INACTIVE)
                          .totalTransactionSum(new AtomicDouble(0))
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
