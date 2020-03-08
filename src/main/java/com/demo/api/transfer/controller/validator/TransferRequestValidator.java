package com.demo.api.transfer.controller.validator;

import com.demo.api.transfer.model.TransferId;
import com.demo.api.transfer.dto.NewTransferRequest;
import com.demo.api.exception.InvalidDataException;
import com.google.inject.Singleton;
import io.javalin.http.Context;

import java.math.BigDecimal;

@Singleton
public class TransferRequestValidator {
    public NewTransferRequest validateNewTransferRequest(Context ctx) {
        NewTransferRequest request = ctx.bodyAsClass(NewTransferRequest.class);
        String sourceAccountId = request.getSourceAccountId();
        if (sourceAccountId == null) {
            throw new InvalidDataException("Source account must be provided");
        }

        String targetAccountId = request.getTargetAccountId();
        if (targetAccountId == null) {
            throw new InvalidDataException("Target account must be provided");
        }

        BigDecimal amount = request.getAmount();
        if (amount == null) {
            throw new InvalidDataException("Amount must be provided");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidDataException("Insufficient transfer amount");
        }

        return request;
    }

    public TransferId validateFindTransferByIdRequest(Context ctx) {
        String transferId = ctx.pathParam("transferId", String.class).getValue();
        if (transferId == null) {
            throw new InvalidDataException("Transfer id must be provided");
        }

        return TransferId.valueOf(transferId);
    }
}
