package com.demo.api.transfer.controller;

import com.demo.api.account.model.AccountId;
import com.demo.api.transfer.model.TransferId;
import com.demo.api.transfer.controller.validator.TransferRequestValidator;
import com.demo.api.transfer.dto.NewTransferRequest;
import com.demo.api.transfer.dto.TransferIdResponse;
import com.demo.api.transfer.dto.TransferResponse;
import com.demo.api.transfer.model.Transfer;
import com.demo.api.transfer.service.TransferService;
import com.demo.api.common.ErrorResponse;
import com.demo.api.exception.NotFoundException;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.javalin.http.Context;
import io.javalin.plugin.openapi.annotations.*;
import org.eclipse.jetty.http.HttpStatus;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Singleton
public class TransferController {
    private final TransferRequestValidator validator;
    private final TransferService transferService;

    @Inject
    public TransferController(TransferRequestValidator requestValidator,
                              TransferService transferService) {
        this.validator = requestValidator;
        this.transferService = Objects.requireNonNull(transferService, "Transfer service must be provided");
    }

    @OpenApi(
            summary = "Create transfer",
            path = "/transfers",
            method = HttpMethod.POST,
            tags = {"Transfer"},
            requestBody = @OpenApiRequestBody(content = {@OpenApiContent(from = NewTransferRequest.class)}),
            responses = {
                    @OpenApiResponse(status = "201", content = {@OpenApiContent(from = TransferIdResponse.class)}),
                    @OpenApiResponse(status = "400", content = {@OpenApiContent(from = ErrorResponse.class)}),
                    @OpenApiResponse(status = "429", content = {@OpenApiContent(from = ErrorResponse.class)})
            }
    )
    public void createNewTransfer(Context ctx) {
        NewTransferRequest request = validator.validateNewTransferRequest(ctx);
        TransferId transferId = transferService.createNew(AccountId.valueOf(request.getSourceAccountId()),
                AccountId.valueOf(request.getTargetAccountId()), request.getAmount());

        ctx.header("Location", String.format("/transfers/%s", transferId.getValue()));
        ctx.json(new TransferIdResponse(transferId.getValue()));
        ctx.status(HttpStatus.CREATED_201);
    }

    @OpenApi(
            summary = "Get transfer by ID",
            path = "/transfers/:transferId",
            method = HttpMethod.GET,
            pathParams = {@OpenApiParam(name = "transferId", description = "The transfer ID")},
            tags = {"Transfer"},
            responses = {
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = TransferResponse.class)}),
                    @OpenApiResponse(status = "400", content = {@OpenApiContent(from = ErrorResponse.class)}),
                    @OpenApiResponse(status = "404", content = {@OpenApiContent(from = ErrorResponse.class)})
            }
    )
    public void findTransferById(Context ctx) {
        TransferId transferId = validator.validateFindTransferByIdRequest(ctx);
        Transfer transfer = transferService.findById(transferId);
        if (transfer == null) {
            throw new NotFoundException(String.format("Transfer %s not found", transferId));
        }

        ctx.json(toTransferResponse(transfer));
        ctx.status(HttpStatus.OK_200);
    }

    @OpenApi(
            summary = "Get all transfers",
            path = "/transfers",
            method = HttpMethod.GET,
            tags = {"Transfer"},
            responses = {
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = TransferResponse.class, isArray = true)})
            }
    )
    public void findAllTransfers(Context ctx) {
        List<TransferResponse> response = transferService.findAll().stream()
                .map(this::toTransferResponse)
                .collect(Collectors.toList());
        ctx.json(response);
        ctx.status(HttpStatus.OK_200);
    }

    private TransferResponse toTransferResponse(Transfer transfer) {
        return new TransferResponse(transfer.getId().getValue(),
                transfer.getSourceId().getValue(), transfer.getTargetId().getValue(),
                transfer.getAmount(), transfer.getState().name());
    }
}
