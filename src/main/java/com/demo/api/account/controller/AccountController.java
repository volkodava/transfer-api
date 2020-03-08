package com.demo.api.account.controller;

import com.demo.api.account.controller.validator.AccountRequestValidator;
import com.demo.api.account.dto.AccountResponse;
import com.demo.api.account.dto.NewAccountRequest;
import com.demo.api.account.model.Account;
import com.demo.api.account.service.AccountService;
import com.demo.api.model.AccountId;
import com.demo.api.transfer.dto.TransferIdResponse;
import com.demo.common.ErrorResponse;
import com.demo.common.NotFoundException;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.javalin.http.Context;
import io.javalin.plugin.openapi.annotations.*;
import org.eclipse.jetty.http.HttpStatus;

import java.util.Objects;

@Singleton
public class AccountController {
    private final AccountService accountService;
    private final AccountRequestValidator requestValidator;

    @Inject
    public AccountController(AccountRequestValidator requestValidator,
                             AccountService accountService) {
        this.requestValidator = Objects.requireNonNull(requestValidator, "Request validator must be provided");
        this.accountService = Objects.requireNonNull(accountService, "Account service must be provided");
    }

    @OpenApi(
            summary = "Create account",
            path = "/accounts",
            method = HttpMethod.POST,
            tags = {"Account"},
            requestBody = @OpenApiRequestBody(content = {@OpenApiContent(from = NewAccountRequest.class)}),
            responses = {
                    @OpenApiResponse(status = "201", content = {@OpenApiContent(from = TransferIdResponse.class)}),
                    @OpenApiResponse(status = "400", content = {@OpenApiContent(from = ErrorResponse.class)})
            }
    )
    public void createNewAccount(Context ctx) {
        NewAccountRequest request = requestValidator.validateNewAccountRequest(ctx);
        AccountId accountId = accountService.createNew(request.getInitialBalance());

        ctx.header("Location", String.format("/accounts/%s", accountId.getValue()));
        ctx.json(new TransferIdResponse(accountId.getValue()));
        ctx.status(HttpStatus.CREATED_201);
    }

    @OpenApi(
            summary = "Get account by ID",
            path = "/accounts/:accountId",
            method = HttpMethod.GET,
            pathParams = {@OpenApiParam(name = "accountId", description = "The account ID")},
            tags = {"Account"},
            responses = {
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = AccountResponse.class)}),
                    @OpenApiResponse(status = "400", content = {@OpenApiContent(from = ErrorResponse.class)}),
                    @OpenApiResponse(status = "404", content = {@OpenApiContent(from = ErrorResponse.class)})
            }
    )
    public void findAccountById(Context ctx) {
        AccountId accountId = requestValidator.validateFindAccountByIdRequest(ctx);
        Account account = accountService.findById(accountId);
        if (account == null) {
            throw new NotFoundException(String.format("Account %s not found", accountId));
        }

        AccountResponse response = new AccountResponse(account.getId().getValue(), account.getBalance());
        ctx.json(response);
        ctx.status(HttpStatus.OK_200);
    }
}
