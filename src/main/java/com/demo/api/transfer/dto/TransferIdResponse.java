package com.demo.api.transfer.dto;

public class TransferIdResponse {
    private String id;

    public TransferIdResponse() {
    }

    public TransferIdResponse(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
