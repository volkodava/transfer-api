package com.demo.api.transfer.controller;

import com.demo.api.account.dto.AccountIdResponse;
import com.demo.api.account.dto.AccountResponse;
import com.demo.api.account.dto.NewAccountRequest;
import com.demo.api.common.ApiResponse;
import com.demo.api.common.TestContext;
import com.demo.api.common.TestHttpClient;
import com.demo.api.transfer.dto.NewTransferRequest;
import com.demo.api.transfer.dto.TransferIdResponse;
import com.demo.api.transfer.dto.TransferResponse;
import com.demo.api.transfer.model.TransferState;
import com.demo.common.BootstrapConfig;
import com.demo.util.SocketUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertTrue;

public class TransferControllerIT {

    private static final String TRANSFERS_PATH = "/transfers";
    private static final String ACCOUNTS_PATH = "/accounts";

    private BootstrapConfig config;
    private TestContext context;
    private TestHttpClient httpClient;

    @Before
    public void beforeTests() {
        config = BootstrapConfig.builder()
                .withPort(SocketUtils.findAvailablePort())
                .withBufferSize(1)
                .withMaxThreads(1)
                .build();
        context = new TestContext(config);
        context.init();
        httpClient = new TestHttpClient(context);
    }

    @After
    public void afterTests() {
        context.destroy();
    }

    @Test
    public void shouldAcceptTransferIfSenderWithSufficientBalance() {
        // Given
        BigDecimal amount = BigDecimal.ONE;
        BigDecimal sourceInitialBalance = BigDecimal.TEN;
        BigDecimal targetInitialBalance = BigDecimal.ZERO;
        ApiResponse<AccountIdResponse> sourceAccountResponse = requestNewAccount(sourceInitialBalance);
        ApiResponse<AccountIdResponse> targetAccountResponse = requestNewAccount(targetInitialBalance);
        NewTransferRequest newTransferRequest = new NewTransferRequest(sourceAccountResponse.getBody().getId(),
                targetAccountResponse.getBody().getId(), amount);

        // When
        ApiResponse<TransferIdResponse> response = httpClient.post(TRANSFERS_PATH, newTransferRequest, TransferIdResponse.class);

        // Then
        assertThat(response.getStatus(), is(HttpStatus.CREATED_201));
        TransferIdResponse transferId = response.getBody();
        assertThat(transferId.getId(), not(isEmptyOrNullString()));
        assertTrue(response.getHeaders().containsKey("Location"));
        String location = response.getFirstHeader("Location");
        assertThat(location, containsString(transferId.getId()));
        // validate if money transferred
        waitForTransferCondition(response, transferResponse -> transferResponse != null
                && TransferState.DONE.name().equals(transferResponse.getState()));
        validateAccountBalance(sourceAccountResponse, sourceInitialBalance.subtract(amount));
        validateAccountBalance(targetAccountResponse, targetInitialBalance.add(amount));
    }

    @Test
    public void shouldRejectTransferIfSenderTransferMoreThanAvailable() {
        // Given
        AccountIdResponse sourceAccountId = requestNewAccount(BigDecimal.ONE).getBody();
        AccountIdResponse targetAccountId = requestNewAccount(BigDecimal.ZERO).getBody();
        BigDecimal amount = BigDecimal.TEN;
        NewTransferRequest newTransferRequest = new NewTransferRequest(sourceAccountId.getId(), targetAccountId.getId(), amount);

        // When
        ApiResponse<TransferIdResponse> response = httpClient.post(TRANSFERS_PATH, newTransferRequest, TransferIdResponse.class);

        // Then
        assertThat(response.getStatus(), is(HttpStatus.BAD_REQUEST_400));
    }

    @Test
    public void shouldRejectTransferIfSenderSendZeroAmount() {
        // Given
        AccountIdResponse sourceAccountId = requestNewAccount(BigDecimal.ONE).getBody();
        AccountIdResponse targetAccountId = requestNewAccount(BigDecimal.ZERO).getBody();
        BigDecimal amount = BigDecimal.ZERO;
        NewTransferRequest newTransferRequest = new NewTransferRequest(sourceAccountId.getId(), targetAccountId.getId(), amount);

        // When
        ApiResponse<TransferIdResponse> response = httpClient.post(TRANSFERS_PATH, newTransferRequest, TransferIdResponse.class);

        // Then
        assertThat(response.getStatus(), is(HttpStatus.BAD_REQUEST_400));
    }

    @Test
    public void shouldRejectTransferIfSenderSendNegativeAmount() {
        // Given
        AccountIdResponse sourceAccountId = requestNewAccount(BigDecimal.ONE).getBody();
        AccountIdResponse targetAccountId = requestNewAccount(BigDecimal.ZERO).getBody();
        BigDecimal amount = BigDecimal.ONE.negate();
        NewTransferRequest newTransferRequest = new NewTransferRequest(sourceAccountId.getId(), targetAccountId.getId(), amount);

        // When
        ApiResponse<TransferIdResponse> response = httpClient.post(TRANSFERS_PATH, newTransferRequest, TransferIdResponse.class);

        // Then
        assertThat(response.getStatus(), is(HttpStatus.BAD_REQUEST_400));
    }

    @Test
    public void shouldRejectTransferIfSenderAndRecipientSameAccounts() {
        // Given
        AccountIdResponse accountId = requestNewAccount(BigDecimal.TEN).getBody();
        BigDecimal amount = BigDecimal.ONE;
        NewTransferRequest newTransferRequest = new NewTransferRequest(accountId.getId(), accountId.getId(), amount);

        // When
        ApiResponse<TransferIdResponse> response = httpClient.post(TRANSFERS_PATH, newTransferRequest, TransferIdResponse.class);

        // Then
        assertThat(response.getStatus(), is(HttpStatus.BAD_REQUEST_400));
    }

    @Test
    public void shouldReturnTransferInformationIfTransferCreated() {
        // Given
        AccountIdResponse sourceAccountId = requestNewAccount(BigDecimal.ONE).getBody();
        AccountIdResponse targetAccountId = requestNewAccount(BigDecimal.ZERO).getBody();
        BigDecimal amount = BigDecimal.ONE;
        NewTransferRequest newTransferRequest = new NewTransferRequest(sourceAccountId.getId(), targetAccountId.getId(), amount);

        // When
        ApiResponse<TransferIdResponse> response = httpClient.post(TRANSFERS_PATH, newTransferRequest, TransferIdResponse.class);

        // Then
        assertThat(response.getStatus(), is(HttpStatus.CREATED_201));
        waitForTransferCondition(response, Objects::nonNull);
    }

    @Test
    public void shouldExecuteTransferIfBelowThreshold() {
        // Given
        assertThat(config.getBufferSize(), is(1));
        AccountIdResponse sourceAccountId = requestNewAccount(BigDecimal.ONE).getBody();
        AccountIdResponse targetAccountId = requestNewAccount(BigDecimal.ZERO).getBody();
        BigDecimal amount = BigDecimal.ONE;
        NewTransferRequest newTransferRequest = new NewTransferRequest(sourceAccountId.getId(), targetAccountId.getId(), amount);

        // When
        ApiResponse<TransferIdResponse> response = httpClient.post(TRANSFERS_PATH, newTransferRequest, TransferIdResponse.class);

        // Then
        assertThat(response.getStatus(), is(HttpStatus.CREATED_201));
        waitForTransferCondition(response, transferResponse -> transferResponse != null
                && TransferState.DONE.name().equals(transferResponse.getState()));
    }

    @Test
    public void shouldReplyNotFoundIfTransferNotExists() {
        // Given
        String transferId = "someFakeId";

        // When
        ApiResponse<TransferIdResponse> response = httpClient.get(String.format("%s/%s", TRANSFERS_PATH, transferId), TransferIdResponse.class);

        // Then
        assertThat(response.getStatus(), is(HttpStatus.NOT_FOUND_404));
    }

    @Test
    public void shouldReplyWithListOfTransfers() {
        // Given
        AccountIdResponse sourceAccountResponse = requestNewAccount(BigDecimal.TEN).getBody();
        AccountIdResponse targetAccountResponse = requestNewAccount(BigDecimal.ZERO).getBody();

        // When
        NewTransferRequest newTransferRequest = new NewTransferRequest(sourceAccountResponse.getId(),
                targetAccountResponse.getId(), BigDecimal.ONE);
        httpClient.post(TRANSFERS_PATH, newTransferRequest, TransferIdResponse.class);

        // Then
        AtomicReference<ApiResponse<TransferResponse[]>> response = new AtomicReference<>();
        await().until(() -> {
            response.set(httpClient.get(TRANSFERS_PATH, TransferResponse[].class));
            // wait for transfer
            return response.get().getBody().length == 1;
        });
        assertThat(response.get().getStatus(), is(HttpStatus.OK_200));
    }

    private void validateAccountBalance(ApiResponse<AccountIdResponse> response, BigDecimal expectedBalance) {
        String accountLocation = response.getFirstHeader("Location");
        AccountResponse account = httpClient.get(accountLocation, AccountResponse.class).getBody();
        assertThat(account.getBalance(), equalTo(expectedBalance));
    }

    private void waitForTransferCondition(ApiResponse<TransferIdResponse> response,
                                          Predicate<TransferResponse> filter) {
        String transferLocation = response.getFirstHeader("Location");
        await().until(() -> {
            ApiResponse<TransferResponse> transferResponse = httpClient.get(transferLocation, TransferResponse.class);
            TransferResponse transfer = transferResponse.getBody();
            return filter.test(transfer);
        });
    }

    private ApiResponse<AccountIdResponse> requestNewAccount(BigDecimal initialBalance) {
        NewAccountRequest newAccountRequest = new NewAccountRequest(initialBalance);
        return httpClient.post(ACCOUNTS_PATH, newAccountRequest, AccountIdResponse.class);
    }
}
