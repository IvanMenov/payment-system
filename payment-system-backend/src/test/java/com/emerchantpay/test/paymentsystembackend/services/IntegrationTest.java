package com.emerchantpay.test.paymentsystembackend.services;

import static org.junit.jupiter.api.Assertions.*;

import com.emerchantpay.test.paymentsystembackend.model.*;
import com.emerchantpay.test.paymentsystembackend.repositories.TransactionRepository;
import com.emerchantpay.test.paymentsystembackend.services.impl.PrincipalService;
import com.emerchantpay.test.paymentsystembackend.services.impl.PrincipalUserDetailsService;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = {IntegrationTest.Initializer.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@EnableAsync
public class IntegrationTest {
  @Container
  static PostgreSQLContainer<?> postgreSQLContainer =
      new PostgreSQLContainer<>("postgres:latest")
          .withDatabaseName("payment_system")
          .withPassword("mysecretpassword")
          .withUsername("postgres");

  // .withInitScript(("init.sql"));

  public static class Initializer
      implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private String fixJdbcUrl() {
      String appendTC = "tc:";
      String jdbc = postgreSQLContainer.getJdbcUrl();
      String jjbcPart = jdbc.substring(0, 5);
      String rest = jdbc.substring(5);
      return jjbcPart + appendTC + rest;
    }

    @Override
    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
      TestPropertyValues values =
          TestPropertyValues.of(
              "spring.datasource.url=" + fixJdbcUrl(),
              "spring.datasource.password=" + postgreSQLContainer.getPassword(),
              "spring.datasource.username=" + postgreSQLContainer.getUsername(),
              "spring.jpa.hibernate.ddl-auto=create");
      values.applyTo(configurableApplicationContext);
    }
  }

  @Autowired private PrincipalService principalService;

  @Autowired private PrincipalUserDetailsService principalUserDetailsService;

  @Autowired private IPaymentService paymentService;

  @Autowired private TransactionRepository transactionRepository;

  @Autowired private IScheduledTask scheduledCleanup;

  @Test
  @Order(1)
  public void testImportPrincipals() throws URISyntaxException, IOException {
    Path path = Paths.get(getClass().getClassLoader().getResource("principals.csv").toURI());

    principalService.importPrincipalsFromCsv(Files.newInputStream(path));

    assertNotNull(principalUserDetailsService.loadUserByUsername("admin@abv.bg"));
    assertNotNull(principalUserDetailsService.loadUserByUsername("merchant1@abv.bg"));
    assertNotNull(principalUserDetailsService.loadUserByUsername("merchant2@abv.bg"));
  }

  @Test
  @Order(2)
  public void testGetAllMerchants() {
    List<Principal> principalList = principalService.findAllMerchants();
    assertNotNull(principalList);
    assertEquals(principalList.get(0).getEmail(), "merchant1@abv.bg");
    assertEquals(principalList.get(0).getName(), "merchant1");
    assertEquals(principalList.get(0).getDescription(), "merchant1-ood");

    assertEquals(principalList.get(1).getEmail(), "merchant2@abv.bg");
    assertEquals(principalList.get(1).getName(), "merchant2");
    assertEquals(principalList.get(1).getDescription(), "merchant2-ood");
  }

  @Test
  @Order(3)
  public void testActivateMerchants() {
    List<Principal> principalList = principalService.findAllMerchants();
    assertNotNull(principalList);
    for (int i = 0; i < principalList.size(); i++) {
      principalList.get(i).setStatus(Principal.Status.ACTIVE);
      principalService.createOrUpdatePrincipal(principalList.get(i));
    }

    List<Principal> principalListUpdated = principalService.findAllMerchants();

    assertNotNull(principalListUpdated);
    assertEquals(
        principalList.get(0).getStatus().getStatusValue(),
        Principal.Status.ACTIVE.getStatusValue());
    assertEquals(
        principalList.get(1).getStatus().getStatusValue(),
        Principal.Status.ACTIVE.getStatusValue());
  }

  @Test
  @Order(4)
  public void testChargeTransaction() throws InterruptedException {
    PaymentDTO paymentDto = new PaymentDTO();
    paymentDto.setTransactionType(TransactionType.CHARGE);
    paymentDto.setAmount(100.0);
    CustomerDTO customerDTO = new CustomerDTO();
    customerDTO.setCustomerAmount(99999.0);
    customerDTO.setCustomerEmail("customer@gmail.com");
    customerDTO.setCustomerPhone("555+56467898");
    paymentDto.setCustomer(customerDTO);
    Principal principal =
        (Principal) principalUserDetailsService.loadUserByUsername("merchant1@abv.bg");
    Transaction transaction = paymentService.initializeTransaction(principal, paymentDto);

    assertNotNull(transaction);
    assertNotNull(transaction.getUuid());
    assertEquals(transaction.getAmount(), 100.0);
    assertEquals(transaction.getType().getType(), TransactionType.AUTHORIZE.getType());

    paymentService.commenceTransactionProcess(principal, paymentDto, transaction);
    TimeUnit.SECONDS.sleep(35);
    List<Transaction> list =
        principalService.findPrincipalById(principal.getId()).get().getTransactionList();

    assertNotNull(list);
    assertEquals(list.size(), 2);
    assertEquals(list.get(0).getType().getType(), TransactionType.CHARGE.getType());
    assertEquals(
        list.get(0).getStatus().getStatusType(), Transaction.Status.APPROVED.getStatusType());

    assertEquals(list.get(1).getType().getType(), TransactionType.AUTHORIZE.getType());
    assertEquals(
        list.get(1).getStatus().getStatusType(), Transaction.Status.APPROVED.getStatusType());

    assertTrue(
        principalService.findPrincipalById(principal.getId()).get().getTotalTransactionSum().get()
            == 100);
  }

  @Test
  @Order(5)
  public void testRefundTransaction() throws InterruptedException {
    Principal principal =
        (Principal) principalUserDetailsService.loadUserByUsername("merchant1@abv.bg");
    Transaction chargeTransaction =
        principal.getTransactionList().stream()
            .filter(transaction -> transaction.getType() == TransactionType.CHARGE)
            .findAny()
            .get();
    PaymentDTO paymentDto = new PaymentDTO();
    paymentDto.setTransactionType(TransactionType.REFUND);
    paymentDto.setAmount(99.0);
    paymentDto.setReferenceId(chargeTransaction.getUuid());

    Transaction refundTransaction = paymentService.initializeTransaction(principal, paymentDto);
    assertNotNull(refundTransaction);
    assertNotNull(refundTransaction.getUuid());
    assertEquals(refundTransaction.getAmount(), 99.0);
    assertEquals(refundTransaction.getType().getType(), TransactionType.REFUND.getType());
    assertEquals(refundTransaction.getReferenceTransactionUUID(), chargeTransaction.getUuid());
    paymentService.commenceTransactionProcess(principal, paymentDto, refundTransaction);
    TimeUnit.SECONDS.sleep(5);

    List<Transaction> list =
        principalService.findPrincipalById(principal.getId()).get().getTransactionList();

    assertNotNull(list);
    assertEquals(list.size(), 3);

    for (int i = 0; i < list.size(); i++) {
      Transaction tr = list.get(i);
      if (tr.getType() == TransactionType.CHARGE) {
        assertEquals(Transaction.Status.REFUNDED.getStatusType(), tr.getStatus().getStatusType());
      } else if (tr.getType() == TransactionType.REFUND) {
        assertEquals(Transaction.Status.APPROVED.getStatusType(), tr.getStatus().getStatusType());
      } else {
        assertEquals(Transaction.Status.APPROVED.getStatusType(), tr.getStatus().getStatusType());
      }
    }
    assertTrue(
        principalService.findPrincipalById(principal.getId()).get().getTotalTransactionSum().get()
            == 1);
  }

  @Test
  @Order(6)
  public void testReversalTransaction() throws InterruptedException {
    Principal principal =
        (Principal) principalUserDetailsService.loadUserByUsername("merchant1@abv.bg");

    PaymentDTO chargeDto = new PaymentDTO();
    chargeDto.setTransactionType(TransactionType.CHARGE);
    chargeDto.setAmount(50.0);
    CustomerDTO customerDTO = new CustomerDTO();
    customerDTO.setCustomerAmount(1000.0);
    customerDTO.setCustomerEmail("newcustomer@gmail.com");
    customerDTO.setCustomerPhone("555+56462398");
    chargeDto.setCustomer(customerDTO);

    Transaction authorizeTransaction = paymentService.initializeTransaction(principal, chargeDto);
    paymentService.commenceTransactionProcess(principal, chargeDto, authorizeTransaction);
    PaymentDTO reversalDto = new PaymentDTO();
    reversalDto.setTransactionType(TransactionType.REVERSAL);
    reversalDto.setReferenceId(authorizeTransaction.getUuid());

    Transaction reversalTransaction = paymentService.initializeTransaction(principal, reversalDto);
    paymentService.commenceTransactionProcess(principal, reversalDto, reversalTransaction);
    TimeUnit.SECONDS.sleep(40);

    List<Transaction> list =
        principalService.findPrincipalById(principal.getId()).get().getTransactionList();

    assertNotNull(list);

    for (int i = 3; i < list.size(); i++) {
      Transaction tr = list.get(i);
      if (tr.getType() == TransactionType.CHARGE) {
        assertEquals(Transaction.Status.ERROR.getStatusType(), tr.getStatus().getStatusType());
      } else if (tr.getType() == TransactionType.AUTHORIZE) {
        assertEquals(Transaction.Status.REVERSED.getStatusType(), tr.getStatus().getStatusType());
      } else {
        assertEquals(Transaction.Status.APPROVED.getStatusType(), tr.getStatus().getStatusType());
      }
    }
    assertTrue(
        principalService.findPrincipalById(principal.getId()).get().getTotalTransactionSum().get()
            == 1);
  }

  @Test
  @Order(7)
  public void removeOldTransactions() {
    Principal principal =
        (Principal) principalUserDetailsService.loadUserByUsername("merchant1@abv.bg");

    PaymentDTO chargeDto = new PaymentDTO();
    chargeDto.setTransactionType(TransactionType.CHARGE);
    chargeDto.setAmount(50.0);
    CustomerDTO customerDTO = new CustomerDTO();
    customerDTO.setCustomerAmount(1000.0);
    customerDTO.setCustomerEmail("newcustomer@gmail.com");
    customerDTO.setCustomerPhone("555+56462398");
    chargeDto.setCustomer(customerDTO);

    Transaction authorizeTransaction = paymentService.initializeTransaction(principal, chargeDto);
    // artificially move the timestamp to 2 hours ago
    long twoHourAgo = 2 * 60 * 60 * 1000;
    long updateTimeStamp = authorizeTransaction.getTimestamp() - twoHourAgo;
    authorizeTransaction.setTimestamp(updateTimeStamp);
    transactionRepository.save(authorizeTransaction);

    List<Transaction> list =
        principalService.findPrincipalById(principal.getId()).get().getTransactionList();

    assertNotNull(list);
    Optional<Transaction> transaction =
        list.stream().filter(tr -> tr.getUuid().equals(authorizeTransaction.getUuid())).findFirst();
    assertTrue(transaction.isPresent());

    int currentSize = list.size();
    scheduledCleanup.runTask();

    List<Transaction> updateList =
        principalService.findPrincipalById(principal.getId()).get().getTransactionList();

    assertTrue(updateList.size() == list.size() - 1);
  }

  @Test
  @Order(8)
  public void deleteMerchant() {

    List<Principal> listMerchants = principalService.findAllMerchants();
    assertNotNull(listMerchants);
    assertTrue(listMerchants.size() == 2);

    for (int i = 0; i < listMerchants.size(); i++) {
      // there are transactions related to that merchant so deletion is expected to fail
      Principal principal = listMerchants.get(i);
      if (principal.getEmail().equals("merchant1@abv.bg")) {
        assertThrows(
            RuntimeException.class,
            () -> principalService.deleteMerchantById(principal.getId()),
            "Cannot delete merchant, because there are related transactions with him/her.");
      } else {
        principalService.deleteMerchantById(principal.getId());
        List<Principal> updateList = principalService.findAllMerchants();

        assertNotNull(updateList);
        assertTrue(updateList.size() == 1);
      }
    }
  }
}
