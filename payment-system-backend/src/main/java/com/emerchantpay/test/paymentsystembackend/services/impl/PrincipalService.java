package com.emerchantpay.test.paymentsystembackend.services.impl;

import com.emerchantpay.test.paymentsystembackend.model.Principal;
import com.emerchantpay.test.paymentsystembackend.model.PrincipalType;
import com.emerchantpay.test.paymentsystembackend.repositories.PrincipalRepository;
import com.emerchantpay.test.paymentsystembackend.repositories.TransactionRepository;
import com.emerchantpay.test.paymentsystembackend.services.IAuthenticationService;
import com.emerchantpay.test.paymentsystembackend.services.IImportPrincipalService;
import com.emerchantpay.test.paymentsystembackend.services.IPrincipalService;
import com.google.common.util.concurrent.AtomicDouble;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PrincipalService implements IPrincipalService<Principal>, IImportPrincipalService {

  @Autowired private PrincipalRepository principalRepository;

  @Autowired private IAuthenticationService authenticationService;

  @Autowired private TransactionRepository transactionRepository;

  /**
   * @param merchant
   *     <p>creates Principal if it doesn't already exist, otherwise updates it
   */
  @Override
  public Principal createOrUpdatePrincipal(Principal merchant) {
    return principalRepository.save(merchant);
  }

  /**
   * finds all Principal with that have PrincipalType.MERCHANT
   *
   * @return List<Principal>
   */
  @Override
  public List<Principal> findAllMerchants() {
    return principalRepository.findAllByPrincipalType(PrincipalType.MERCHANT);
  }

  /**
   * @param id delete merchant by id by first if there are no transactions related to that merchant
   *     otherwise the method would throw a RuntimeException exception
   */
  @Override
  @Transactional
  public void deleteMerchantById(long id) {
    if (transactionRepository.hasTransactionForMerchant(id) > 0) {
      throw new RuntimeException(
          "Cannot delete merchant, because there are related transactions with him/her.");
    }
    principalRepository.deleteById(id);
  }

  /**
   * @param merchant check if merchant is of PrincipalType.MERCHANT and has
   *     Principal.Status.INACTIVE
   * @return boolean
   */
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

  /**
   * @param id finds Principal by id
   * @return Optional<Principal>
   */
  @Override
  public Optional<Principal> findPrincipalById(long id) {
    return principalRepository.findById(id);
  }

  /**
   * @param stream insert Principal record in database from valid .csv files in appropriate form
   */
  @Override
  public void importPrincipalsFromCsv(InputStream stream) {
    CSVParser parser = new CSVParserBuilder().withSeparator(',').withIgnoreQuotations(true).build();

    try (Reader reader = new InputStreamReader(stream);
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
                          .name(array[1])
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

                if (principal != null
                    && principalRepository.findByEmail(principal.getEmail()).isEmpty()) {
                  createOrUpdatePrincipal(principal);
                }
              });

    } catch (Exception exception) {
      throw new RuntimeException(exception.getMessage());
    }
  }
}
