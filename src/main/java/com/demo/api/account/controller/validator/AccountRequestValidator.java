package com.demo.api.account.controller.validator;

import com.demo.api.account.dto.NewAccountRequest;
import com.demo.api.account.model.AccountId;
import com.demo.api.exception.InvalidDataException;
import com.google.inject.Singleton;
import io.javalin.http.Context;

import java.math.BigDecimal;

@Singleton
public class AccountRequestValidator {
    public NewAccountRequest validateNewAccountRequest(Context ctx) {
        NewAccountRequest request = ctx.bodyAsClass(NewAccountRequest.class);
        BigDecimal initialBalance = request.getInitialBalance();
        if (initialBalance == null) {
            throw new InvalidDataException("Account balance must be provided");
        }
        if (initialBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidDataException("Insufficient account balance");
        }

        return request;
    }

    public AccountId validateFindAccountByIdRequest(Context ctx) {
        String accountId = ctx.pathParam("accountId", String.class).getValue();
        if (accountId == null) {
            throw new InvalidDataException("Account id must be provided");
        }

        return AccountId.valueOf(accountId);
    }
}
