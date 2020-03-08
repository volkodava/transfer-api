package com.demo.api.account.dto;

public class AccountIdResponse {
    private String id;

    public AccountIdResponse() {
    }

    public AccountIdResponse(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
