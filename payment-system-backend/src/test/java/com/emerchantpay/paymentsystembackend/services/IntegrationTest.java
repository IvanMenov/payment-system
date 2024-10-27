package com.emerchantpay.paymentsystembackend.services;

import static org.junit.jupiter.api.Assertions.*;

import com.emerchantpay.paymentsystembackend.model.*;
import com.emerchantpay.paymentsystembackend.repositories.TransactionRepository;
import com.emerchantpay.paymentsystembackend.services.impl.PrincipalService;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@EnableAsync
public class IntegrationTest {

  @Container
  static PostgreSQLContainer<?> postgreSQLContainer =
      new PostgreSQLContainer<>("postgres:latest")
          .withDatabaseName("payment_system")
          .withPassword("mysecretpassword")
          .withUsername("postgres");
  @Container
  private static final KafkaContainer kafkaContainer = new KafkaContainer(
          DockerImageName.parse("confluentinc/cp-kafka:7.4.0")
  );

  private static String fixJdbcUrl() {
    String appendTC = "tc:";
    String jdbc = postgreSQLContainer.getJdbcUrl();
    String jjbcPart = jdbc.substring(0, 5);
    String rest = jdbc.substring(5);
    return jjbcPart + appendTC + rest;
  }

  @BeforeAll
  public static void setUp() {
    // Configure Spring Kafka to use the Kafka container's bootstrap servers
    System.setProperty("spring.kafka.bootstrap-servers", kafkaContainer.getBootstrapServers());
    System.setProperty("spring.datasource.url", fixJdbcUrl());
    System.setProperty("spring.datasource.password", postgreSQLContainer.getPassword());
    System.setProperty("spring.datasource.username", postgreSQLContainer.getUsername());
    System.setProperty("spring.jpa.hibernate.ddl-auto", "create");
  }

  @Autowired
  private KafkaTemplate<String, String> kafkaTemplate;

  @Autowired private PrincipalService principalService;


  @Autowired private IPaymentService paymentService;

  @Autowired private TransactionRepository transactionRepository;

  @Autowired private IScheduledTask scheduledCleanup;



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
      principalList.get(i).setPrincipalStatus(Principal.Status.ACTIVE);
      principalService.createOrUpdatePrincipal(principalList.get(i));
    }

    List<Principal> principalListUpdated = principalService.findAllMerchants();

    assertNotNull(principalListUpdated);
    assertEquals(
        principalList.get(0).getPrincipalStatus().getStatusValue(),
        Principal.Status.ACTIVE.getStatusValue());
    assertEquals(
        principalList.get(1).getPrincipalStatus().getStatusValue(),
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
    Principal principal = principalService.findPrincipalByEmail("merchant1@abv.bg").get();
    Transaction transaction = paymentService.initializeTransaction(principal, paymentDto);

    assertNotNull(transaction);
    assertNotNull(transaction.getUuid());
    assertEquals(transaction.getAmount(), 100.0);
    assertEquals(transaction.getTransactionType().getType(), TransactionType.AUTHORIZE.getType());

    paymentService.commenceTransactionProcess(principal, paymentDto, transaction);
    TimeUnit.SECONDS.sleep(35);
    List<Transaction> list =
        principalService.findPrincipalById(principal.getId()).get().getTransactionList();

    assertNotNull(list);
    assertEquals(list.size(), 2);
    assertEquals(list.get(0).getTransactionType().getType(), TransactionType.CHARGE.getType());
    assertEquals(
        list.get(0).getTransactionStatus().getStatusType(), Transaction.Status.APPROVED.getStatusType());

    assertEquals(list.get(1).getTransactionType().getType(), TransactionType.AUTHORIZE.getType());
    assertEquals(
        list.get(1).getTransactionStatus().getStatusType(), Transaction.Status.APPROVED.getStatusType());

    assertTrue(
        principalService
                .findPrincipalById(principal.getId())
                .get()
                .getTotalTransactionSum()
                .doubleValue()
            == 100.0);
  }

  @Test
  @Order(5)
  public void testRefundTransaction() throws InterruptedException {
    Principal principal = principalService.findPrincipalByEmail("merchant1@abv.bg").get();
    Transaction chargeTransaction =
        principal.getTransactionList().stream()
            .filter(transaction -> transaction.getTransactionType() == TransactionType.CHARGE)
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
    assertEquals(refundTransaction.getTransactionType().getType(), TransactionType.REFUND.getType());
    assertEquals(refundTransaction.getReferenceTransactionUUID(), chargeTransaction.getUuid());
    paymentService.commenceTransactionProcess(principal, paymentDto, refundTransaction);
    TimeUnit.SECONDS.sleep(5);

    List<Transaction> list =
        principalService.findPrincipalById(principal.getId()).get().getTransactionList();

    assertNotNull(list);
    assertEquals(list.size(), 3);

    for (int i = 0; i < list.size(); i++) {
      Transaction tr = list.get(i);
      if (tr.getTransactionType() == TransactionType.CHARGE) {
        assertEquals(Transaction.Status.REFUNDED.getStatusType(), tr.getTransactionStatus().getStatusType());
      } else if (tr.getTransactionType() == TransactionType.REFUND) {
        assertEquals(Transaction.Status.APPROVED.getStatusType(), tr.getTransactionStatus().getStatusType());
      } else {
        assertEquals(Transaction.Status.APPROVED.getStatusType(), tr.getTransactionStatus().getStatusType());
      }
    }
    assertTrue(
        principalService
                .findPrincipalById(principal.getId())
                .get()
                .getTotalTransactionSum()
                .doubleValue()
            == 1);
  }

  @Test
  @Order(6)
  public void testReversalTransaction() throws InterruptedException {
    Principal principal = principalService.findPrincipalByEmail("merchant1@abv.bg").get();

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
      if (tr.getTransactionType() == TransactionType.CHARGE) {
        assertEquals(Transaction.Status.ERROR.getStatusType(), tr.getTransactionStatus().getStatusType());
      } else if (tr.getTransactionType() == TransactionType.AUTHORIZE) {
        assertEquals(Transaction.Status.REVERSED.getStatusType(), tr.getTransactionStatus().getStatusType());
      } else {
        assertEquals(Transaction.Status.APPROVED.getStatusType(), tr.getTransactionStatus().getStatusType());
      }
    }
    assertTrue(
        principalService
                .findPrincipalById(principal.getId())
                .get()
                .getTotalTransactionSum()
                .doubleValue()
            == 1);
  }

  @Test
  @Order(7)
  public void removeOldTransactions() {
    Principal principal = principalService.findPrincipalByEmail("merchant1@abv.bg").get();

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
