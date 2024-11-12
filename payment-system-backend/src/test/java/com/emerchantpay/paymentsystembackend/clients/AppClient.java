package com.emerchantpay.paymentsystembackend.clients;

import com.emerchantpay.paymentsystembackend.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class AppClient {
    private static TestRestTemplate testRestTemplate = new TestRestTemplate();

    public static Principal signIn(KeycloakToken token, int port){
        HttpHeaders headers = new HttpHeaders();
        String bearerToken = "Bearer "+token.getAccessToken();
        headers.add("Authorization",bearerToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        String url = "http://localhost:"+ port + "/api/v1/principal/signin";
        return testRestTemplate.exchange(url, HttpMethod.GET,entity, Principal.class).getBody();
    }
    public static ResponseEntity<List<Principal>> getMerchants(KeycloakToken token, int port){
        HttpHeaders headers = new HttpHeaders();
        String bearerToken = "Bearer "+token.getAccessToken();
        headers.add("Authorization",bearerToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        String url = "http://localhost:"+ port + "/api/v1/admin/merchants";
        ParameterizedTypeReference<List<Principal>> responseType = new ParameterizedTypeReference<>() {};
        return testRestTemplate.exchange(url, HttpMethod.GET,entity, responseType);
    }
    public static Principal getMerchant(KeycloakToken token, int port, long merchantId, int limit, int offset) {
        HttpHeaders headers = new HttpHeaders();
        String bearerToken = "Bearer "+token.getAccessToken();
        headers.add("Authorization",bearerToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        String url = String.format("http://localhost:%d/api/v1/merchants/%d?limit=%d&offset=%d", port ,merchantId, limit,offset);
        return testRestTemplate.exchange(url, HttpMethod.GET,entity, Principal.class).getBody();
    }

    public static Principal changeMerchantStatus(KeycloakToken token, int port, int merchantId, PrincipalStatus status){
        HttpHeaders headers = new HttpHeaders();
        String bearerToken = "Bearer "+token.getAccessToken();
        headers.add("Authorization",bearerToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        String url = String.format("http://localhost:%d/api/v1/admin/merchants/%d/status/%s", port, merchantId, status.getStatusValue().toUpperCase());
        return testRestTemplate.exchange(url, HttpMethod.PUT,entity, Principal.class).getBody();
    }

    public static ResponseEntity<Void> initTransaction(KeycloakToken token, int port, long merchantId, Payment payment ){
        HttpHeaders headers = new HttpHeaders();
        String bearerToken = "Bearer "+token.getAccessToken();
        headers.add("Authorization",bearerToken);
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
        ObjectMapper mapper = new ObjectMapper();
        try {
            String body = mapper.writeValueAsString(payment);
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        String url = String.format("http://localhost:%d/api/v1/payment/transactions/init/%d",port, merchantId);
        return testRestTemplate.postForEntity(url,entity, Void.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static Transaction monitorTransaction(KeycloakToken token, int port, String url){
        HttpHeaders headers = new HttpHeaders();
        String bearerToken = "Bearer "+token.getAccessToken();
        headers.add("Authorization",bearerToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        return testRestTemplate.exchange(url, HttpMethod.GET,entity, Transaction.class).getBody();
    }
}
