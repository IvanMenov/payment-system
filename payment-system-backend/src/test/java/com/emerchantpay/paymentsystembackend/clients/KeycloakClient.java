package com.emerchantpay.paymentsystembackend.clients;

import com.emerchantpay.paymentsystembackend.model.KeycloakToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class KeycloakClient {
    private static TestRestTemplate testRestTemplate = new TestRestTemplate();

    public static KeycloakToken getKeycloakToken(String keycloakUrl, String username, String password, String clientId){
        String keycloakTokenEndpoint =  keycloakUrl + "/realms/payment-system-realm/protocol/openid-connect/token";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
        map.add("grant_type", "password");
        map.add("client_id", clientId);
        map.add("username", username);
        map.add("password", password);
        HttpEntity entity = new HttpEntity<>(map, headers);
        return testRestTemplate.postForObject(keycloakTokenEndpoint,entity, KeycloakToken.class );
    }
}
