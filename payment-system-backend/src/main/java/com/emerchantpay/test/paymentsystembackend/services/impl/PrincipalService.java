package com.emerchantpay.test.paymentsystembackend.services.impl;

import com.emerchantpay.test.paymentsystembackend.model.Principal;
import com.emerchantpay.test.paymentsystembackend.model.PrincipalType;
import com.emerchantpay.test.paymentsystembackend.repositories.PrincipalRepository;
import com.emerchantpay.test.paymentsystembackend.repositories.TransactionRepository;
import com.emerchantpay.test.paymentsystembackend.services.IAuthenticationService;
import com.emerchantpay.test.paymentsystembackend.services.IImportPrincipalService;
import com.emerchantpay.test.paymentsystembackend.services.IPrincipalService;
import com.emerchantpay.test.paymentsystembackend.validation.EmailValidation;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.apache.commons.io.input.BOMInputStream;
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

  private List<String> failedPrincipalsList = new ArrayList<>();

  /**
   * @param stream insert Principal record in database from valid .csv files in appropriate form
   */
  @Override
  public void importPrincipalsFromCsv(InputStream stream) {
    // wrap the initial stream and return a new one without ByteOrderMark as its first bytes.
    BOMInputStream bomIn = new BOMInputStream(stream);
    CSVParser parser = new CSVParserBuilder().withSeparator(',').withIgnoreQuotations(true).build();
    try (Reader reader = new InputStreamReader(bomIn, StandardCharsets.UTF_8);
        CSVReader csvReader = new CSVReaderBuilder(reader).withCSVParser(parser).build(); ) {
      List<String[]> lines = csvReader.readAll();
      if (lines.isEmpty()) {
        throw new RuntimeException("File is empty");
      }
      if (!validCsvColumns(lines.get(0))) {
        throw new RuntimeException("Invalid .csv file");
      }
      for (int i = 1; i < lines.size(); i++) {
        Principal principal = null;
        String[] array = lines.get(i);
        if (validateRow(array)) {
          switch (PrincipalType.valueOf(array[0].toUpperCase())) {
            case ADMIN:
              principal =
                  Principal.builder()
                      .name(array[1])
                      .email(array[3])
                      .password(authenticationService.encodePassword(array[4]))
                      .principalType(PrincipalType.ADMIN)
                      .status(Principal.Status.ACTIVE)
                      .build();
              break;
            case MERCHANT:
              principal =
                  Principal.builder()
                      .name(array[1])
                      .description(array[2])
                      .email(array[3])
                      .password(authenticationService.encodePassword(array[4]))
                      .principalType(PrincipalType.MERCHANT)
                      .status(Principal.Status.INACTIVE)
                      .totalTransactionSum(BigDecimal.valueOf(0))
                      .build();
              break;
          }

        } else {
          System.out.println(
              String.format("Failed to validate principal: %s", Arrays.toString(array)));
        }
        if (principal != null && principalRepository.findByEmail(principal.getEmail()).isEmpty()) {
          createOrUpdatePrincipal(principal);
        }
      }

    } catch (Exception exception) {
      throw new RuntimeException(exception.getMessage());
    }
  }

  private boolean validCsvColumns(String[] columns) {
    return columns != null
        && columns.length >= 5
        && "type".equalsIgnoreCase(columns[0].trim())
        && "name".equalsIgnoreCase(columns[1].trim())
        && "description".equalsIgnoreCase(columns[2].trim())
        && "email".equalsIgnoreCase(columns[3].trim())
        && "password".equalsIgnoreCase(columns[4].trim());
  }

  private boolean validateRow(String[] input) {
    return (input[0].equalsIgnoreCase(PrincipalType.ADMIN.getType())
            || input[0].equalsIgnoreCase(PrincipalType.MERCHANT.getType()))
        && EmailValidation.patternMatches(input[3])
        && input[4].length() >= 4;
  }
}
