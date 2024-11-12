package com.emerchantpay.paymentsystembackend.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class KeycloakToken {

    @JsonProperty("access_token")
    private String accessToken;

    public KeycloakToken(){}

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }



}
