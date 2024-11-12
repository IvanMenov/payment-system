package com.emerchantpay.paymentsystembackend.tests;

import static com.emerchantpay.paymentsystembackend.clients.AppClient.*;
import static com.emerchantpay.paymentsystembackend.clients.KeycloakClient.getKeycloakToken;
import static org.junit.jupiter.api.Assertions.*;

import com.emerchantpay.paymentsystembackend.configurations.*;
import com.emerchantpay.paymentsystembackend.model.*;
import com.emerchantpay.paymentsystembackend.repositories.TransactionRepository;
import com.emerchantpay.paymentsystembackend.services.IPaymentService;
import com.emerchantpay.paymentsystembackend.services.IScheduledTask;
import com.emerchantpay.paymentsystembackend.services.impl.PrincipalService;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.EnableAsync;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Import({KafkaConsumerConfig.class, KafkaProducerConfig.class, SecurityConfig.class})
@EnableAsync
public class IntegrationTest {
  private static final PostgreSQLContainer<?> postgreSQLContainer =
          new PostgreSQLContainer<>("postgres:15")
                  .withDatabaseName("payment_system")
                  .withPassword("mysecretpassword")
                  .withUsername("postgres");

  private static final KafkaContainer kafkaContainer = new KafkaContainer(
          DockerImageName.parse("confluentinc/cp-kafka:7.4.0")
  );

  private static final KeycloakContainer keycloak;

  static {
    keycloak = new KeycloakContainer().withRealmImportFile("/realm-export.json");
    keycloak.start();
    postgreSQLContainer.start();
    kafkaContainer.start();
  }

  @BeforeAll
  public static void setUp() {
    // Configure Spring Kafka to use the Kafka container's bootstrap servers
    String issuerUri = String.format("%s/realms/payment-system-realm", keycloak.getAuthServerUrl());
    System.setProperty ("spring.kafka.bootstrap-servers", kafkaContainer.getBootstrapServers());
    System.setProperty("spring.datasource.url", postgreSQLContainer.getJdbcUrl());
    System.setProperty("spring.datasource.password", postgreSQLContainer.getPassword());
    System.setProperty("spring.datasource.username", postgreSQLContainer.getUsername());
    System.setProperty("spring.security.oauth2.resourceserver.jwt.issuer-uri", issuerUri);
    System.setProperty("spring.security.oauth2.resourceserver.jwt.jwk-set-uri",
            issuerUri + "/protocol/openid-connect/certs");

  }
  @Autowired private PrincipalService principalService;

  @Autowired private IPaymentService paymentService;

  @Autowired private TransactionRepository transactionRepository;

  @Autowired private IScheduledTask scheduledCleanup;



  // Inject the random port
  @LocalServerPort
  private int port;

  @Value("${jwt.auth.converter.resource-id}")
  private String clientId;


  @Test
  @Order(1)
  public void testSignIn(){
    adminSignIn();
    merchant1SignIn();
    merchant2SignIn();
  }

  private void merchant2SignIn() {
    KeycloakToken token = getKeycloakToken(keycloak.getAuthServerUrl(), "merchant2@abv.bg", "pass3", clientId);
    Principal merchant =  signIn(token, port);
    assertNotNull(merchant);
    assertEquals("merchant2@abv.bg", merchant.getEmail());
    assertEquals(PrincipalStatus.INACTIVE, merchant.getPrincipalStatus());
    assertEquals(PrincipalType.MERCHANT, merchant.getPrincipalType());
  }

  private void merchant1SignIn() {
    KeycloakToken token = getKeycloakToken(keycloak.getAuthServerUrl(),"merchant1@abv.bg", "pass2", clientId);
    Principal merchant = signIn(token, port);
    assertNotNull(merchant);
    assertEquals("merchant1@abv.bg", merchant.getEmail());
    assertEquals(PrincipalStatus.INACTIVE, merchant.getPrincipalStatus());
    assertEquals(PrincipalType.MERCHANT, merchant.getPrincipalType());
  }

  private void adminSignIn() {
    KeycloakToken token = getKeycloakToken(keycloak.getAuthServerUrl(), "admin@abv.bg", "pass1", clientId);
    Principal admin =  signIn(token,port);
    assertNotNull(admin);
    assertEquals("admin@abv.bg", admin.getEmail());
    assertEquals(PrincipalStatus.ACTIVE, admin.getPrincipalStatus());
    assertEquals(PrincipalType.ADMIN, admin.getPrincipalType());
  }
 @Test
  @Order(2)
  public void testGetAllMerchants() {
   KeycloakToken token = getKeycloakToken(keycloak.getAuthServerUrl(), "admin@abv.bg", "pass1", clientId);
   List<Principal> principalList =getMerchants(token, port).getBody();
    assertNotNull(principalList);
    assertEquals(principalList.get(0).getEmail(), "merchant1@abv.bg");
    assertEquals(principalList.get(1).getEmail(), "merchant2@abv.bg");

    //getMerchants should fail for principals other than admin
   KeycloakToken tokenMerchant = getKeycloakToken(keycloak.getAuthServerUrl(), "merchant1@abv.bg", "pass2", clientId);

   ResponseEntity<?> responseEntity = getMerchants(tokenMerchant, port);
   assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
  }


  @Test
  @Order(3)
  public void testActivateMerchants() {
    KeycloakToken token = getKeycloakToken(keycloak.getAuthServerUrl(), "admin@abv.bg", "pass1", clientId);
    List<Principal> principalList =getMerchants(token, port).getBody();
    assertNotNull(principalList);
      for (Principal principal : principalList) {
          changeMerchantStatus(token, port, principal.getId().intValue(), PrincipalStatus.ACTIVE);
      }

    List<Principal> principalListUpdated = principalService.findAllMerchants();

    assertNotNull(principalListUpdated);
    assertEquals(
            principalListUpdated.get(0).getPrincipalStatus().getStatusValue(),
            PrincipalStatus.ACTIVE.getStatusValue());
    assertEquals(
            principalListUpdated.get(1).getPrincipalStatus().getStatusValue(),
            PrincipalStatus.ACTIVE.getStatusValue());
  }

  @Test
  @Order(4)
  public void testChargeTransaction() throws InterruptedException {
    Payment paymentDto = new Payment();
    paymentDto.setTransactionType(TransactionType.CHARGE);
    paymentDto.setAmount(100.0);
    Customer customerDTO = new Customer();
    customerDTO.setCustomerAmount(99999.0);
    customerDTO.setCustomerEmail("customer@gmail.com");
    customerDTO.setCustomerPhone("0885646789");
    paymentDto.setCustomer(customerDTO);

    KeycloakToken tokenMerchant = getKeycloakToken(keycloak.getAuthServerUrl(), "merchant1@abv.bg", "pass2", clientId);

    Principal principal = principalService.findPrincipalByEmail("merchant1@abv.bg").get();

    ResponseEntity<Void> response = initTransaction(tokenMerchant,port,principal.getId(),paymentDto);
    assertNotNull(response);
    assertNotNull(response.getHeaders().get(HttpHeaders.LOCATION).get(0));
    TimeUnit.SECONDS.sleep(10);
    String monitorTransactionUrl = String.format("http://localhost:%d/%s",port, response.getHeaders().get(HttpHeaders.LOCATION).get(0));
    Transaction transaction = monitorTransaction(tokenMerchant,port,monitorTransactionUrl );

    assertNotNull(transaction.getUuid());
    assertEquals(transaction.getAmount(), 100.0);
    assertEquals(transaction.getTransactionType().getType(), TransactionType.AUTHORIZE.getType());

    TimeUnit.SECONDS.sleep(35);

    Principal merchant = getMerchant(tokenMerchant,port,principal.getId(),10,0);
    assertNotNull(merchant.getTransactionList());
    assertEquals(2, merchant.getTransactionList().size());
    assertEquals(TransactionType.CHARGE.getType(),merchant.getTransactionList().get(0).getTransactionType().getType());
    assertEquals(TransactionStatus.APPROVED.getStatusType(),
            merchant.getTransactionList().get(0).getTransactionStatus().getStatusType());

    assertEquals( TransactionType.AUTHORIZE.getType(),merchant.getTransactionList().get(1).getTransactionType().getType());
    assertEquals(TransactionStatus.APPROVED.getStatusType(),
            merchant.getTransactionList().get(1).getTransactionStatus().getStatusType());

      assertEquals(100.0, merchant.getTotalTransactionSum().doubleValue());
  }

  @Test
  @Order(5)
  public void testRefundTransaction() throws InterruptedException {
    Principal principal = principalService.findPrincipalByEmail("merchant1@abv.bg").get();
    KeycloakToken tokenMerchant = getKeycloakToken(keycloak.getAuthServerUrl(), "merchant1@abv.bg", "pass2", clientId);

    Principal merchant = getMerchant(tokenMerchant,port,principal.getId(),10,0);

    Transaction chargeTransaction =
            merchant.getTransactionList().stream()
            .filter(transaction -> transaction.getTransactionType() == TransactionType.CHARGE)
            .findAny()
            .get();

    Payment paymentDto = new Payment();
    paymentDto.setTransactionType(TransactionType.REFUND);
    paymentDto.setAmount(99.0);
    paymentDto.setReferenceId(chargeTransaction.getUuid());

    ResponseEntity<Void> response = initTransaction(tokenMerchant,port,principal.getId(),paymentDto);

    TimeUnit.SECONDS.sleep(5);
    String monitorTransactionUrl = String.format("http://localhost:%d/%s",port, response.getHeaders().get(HttpHeaders.LOCATION).get(0));
    Transaction transaction = monitorTransaction(tokenMerchant,port,monitorTransactionUrl );

    assertNotNull(transaction);
    assertNotNull(transaction.getUuid());
    assertEquals(transaction.getAmount(), 99.0);
    assertEquals(transaction.getTransactionType().getType(), TransactionType.REFUND.getType());
    assertEquals(transaction.getReferenceTransactionUUID(), chargeTransaction.getUuid());



    Principal merchant1 = getMerchant(tokenMerchant,port,principal.getId(),10,0);

    assertNotNull(merchant1.getTransactionList());
    assertEquals(merchant1.getTransactionList().size(), 3);

    for (int i = 0; i < merchant1.getTransactionList().size(); i++) {
      Transaction tr = merchant1.getTransactionList().get(i);
      if (tr.getTransactionType() == TransactionType.CHARGE) {
        assertEquals(TransactionStatus.REFUNDED.getStatusType(), tr.getTransactionStatus().getStatusType());
      } else if (tr.getTransactionType() == TransactionType.REFUND) {
        assertEquals(TransactionStatus.APPROVED.getStatusType(), tr.getTransactionStatus().getStatusType());
      } else {
        assertEquals(TransactionStatus.APPROVED.getStatusType(), tr.getTransactionStatus().getStatusType());
      }
    }
      assertEquals(1, merchant1.getTotalTransactionSum().doubleValue());
  }

  @Test
  @Order(6)
  public void testReversalTransaction() throws InterruptedException {
    Principal principal = principalService.findPrincipalByEmail("merchant1@abv.bg").get();
    KeycloakToken tokenMerchant = getKeycloakToken(keycloak.getAuthServerUrl(), "merchant1@abv.bg", "pass2", clientId);

    Payment chargeDto = new Payment();
    chargeDto.setTransactionType(TransactionType.CHARGE);
    chargeDto.setAmount(50.0);
    Customer customer = new Customer();
    customer.setCustomerAmount(1000.0);
    customer.setCustomerEmail("newcustomer@gmail.com");
    customer.setCustomerPhone("0856462398");
    chargeDto.setCustomer(customer);

    ResponseEntity<Void> responseCharge = initTransaction(tokenMerchant,port,principal.getId(),chargeDto);
    //extract the UUID from the response LOCATION header
    String[] segments = responseCharge.getHeaders().get(HttpHeaders.LOCATION).get(0).split("/");

    Payment reversalDto = new Payment();
    reversalDto.setTransactionType(TransactionType.REVERSAL);
    //the uuid should be the next to last segment from
    reversalDto.setReferenceId(segments[segments.length-2]);

    ResponseEntity<Void> responseReversal = initTransaction(tokenMerchant,port,principal.getId(),reversalDto);
    TimeUnit.SECONDS.sleep(25);

    Principal merchant = getMerchant(tokenMerchant,port,principal.getId(),10,0);

    assertNotNull(merchant.getTransactionList());
    merchant.getTransactionList().forEach( tr -> {

    });
    List<Transaction>listAuthorize =merchant.getTransactionList().stream()
            .filter(transaction -> transaction.getTransactionType() == TransactionType.AUTHORIZE).toList();

    assertEquals(2, listAuthorize.size());
    assertEquals(TransactionStatus.APPROVED, listAuthorize.get(1).getTransactionStatus());
    assertEquals(TransactionStatus.REVERSED, listAuthorize.get(0).getTransactionStatus());

    List<Transaction>listCharge =merchant.getTransactionList().stream()
            .filter(transaction -> transaction.getTransactionType() == TransactionType.CHARGE).toList();

    assertEquals(2, listCharge.size());
    assertEquals(TransactionStatus.REFUNDED, listCharge.get(1).getTransactionStatus());
    assertEquals(TransactionStatus.ERROR, listCharge.get(0).getTransactionStatus());

    List<Transaction>listRefund =merchant.getTransactionList().stream()
            .filter(transaction -> transaction.getTransactionType() == TransactionType.REFUND).toList();

    assertEquals(1, listRefund.size());
    assertEquals(TransactionStatus.APPROVED, listRefund.get(0).getTransactionStatus());

    List<Transaction>listReversal =merchant.getTransactionList().stream()
            .filter(transaction -> transaction.getTransactionType() == TransactionType.REVERSAL).toList();

    assertEquals(1, listReversal.size());
    assertEquals(TransactionStatus.APPROVED, listReversal.get(0).getTransactionStatus());

    assertEquals(1,merchant.getTotalTransactionSum().doubleValue());
  }

  @Test
  @Order(7)
  public void removeOldTransactions() {
    Principal principal = principalService.findPrincipalByEmail("merchant1@abv.bg").get();

    Payment chargeDto = new Payment();
    chargeDto.setTransactionType(TransactionType.CHARGE);
    chargeDto.setAmount(50.0);
    Customer customerDTO = new Customer();
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
        principalService.findPrincipalByIdLoadBatchOfTransactions(principal.getId(),10, 0).get().getTransactionList();

    assertNotNull(list);
    Optional<Transaction> transaction =
        list.stream().filter(tr -> tr.getUuid().equals(authorizeTransaction.getUuid())).findFirst();
    assertTrue(transaction.isPresent());

    int currentSize = list.size();
    scheduledCleanup.runTask();

    List<Transaction> updateList =
            principalService.findPrincipalByIdLoadBatchOfTransactions(principal.getId(),10, 0).get().getTransactionList();

      assertEquals(updateList.size(), list.size() - 1);

      Optional<Transaction> tr  = transactionRepository.findById(authorizeTransaction.getUuid());

      assertTrue(tr.isEmpty());
  }

  @Test
  @Order(8)
  public void deleteMerchant() {
    List<Principal> listMerchants = principalService.findAllMerchants();
    assertNotNull(listMerchants);
    assertEquals(2, listMerchants.size());

      for (Principal principal : listMerchants) {
          // there are transactions related to that merchant so deletion is expected to fail
          if (principal.getEmail().equals("merchant1@abv.bg")) {
              assertThrows(
                      RuntimeException.class,
                      () -> principalService.deleteMerchantById(principal.getId()),
                      "Cannot delete merchant, because there are related transactions with him/her.");
          } else {
              principalService.deleteMerchantById(principal.getId());
              List<Principal> updateList = principalService.findAllMerchants();

              assertNotNull(updateList);
              assertEquals(1, updateList.size());
          }
      }
  }
}
