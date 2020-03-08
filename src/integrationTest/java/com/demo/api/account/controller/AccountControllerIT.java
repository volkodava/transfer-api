package com.demo.api.account.controller;

import com.demo.api.account.dto.AccountIdResponse;
import com.demo.api.account.dto.AccountResponse;
import com.demo.api.account.dto.NewAccountRequest;
import com.demo.api.common.ApiResponse;
import com.demo.api.common.TestContext;
import com.demo.api.common.TestHttpClient;
import com.demo.common.BootstrapConfig;
import com.demo.common.SocketUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertTrue;

public class AccountControllerIT {

    private static final String ACCOUNTS_PATH = "/accounts";

    private TestContext context;
    private TestHttpClient httpClient;

    @Before
    public void beforeTests() {
        BootstrapConfig config = BootstrapConfig.builder()
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
    public void shouldCreateAccountWithPositiveBalance() {
        // Given
        NewAccountRequest newAccountRequest = new NewAccountRequest(BigDecimal.ONE);

        // When
        ApiResponse<AccountIdResponse> response = httpClient.post(ACCOUNTS_PATH, newAccountRequest, AccountIdResponse.class);

        // Then
        assertThat(response.getStatus(), is(HttpStatus.CREATED_201));
        AccountIdResponse accountId = response.getBody();
        assertThat(accountId.getId(), not(isEmptyOrNullString()));
        String location = response.getFirstHeader("Location");
        assertThat(location, containsString(accountId.getId()));
    }

    @Test
    public void shouldCreateAccountWithZeroBalance() {
        // Given
        NewAccountRequest newAccountRequest = new NewAccountRequest(BigDecimal.ZERO);

        // When
        ApiResponse<AccountIdResponse> response = httpClient.post(ACCOUNTS_PATH, newAccountRequest, AccountIdResponse.class);

        // Then
        assertThat(response.getStatus(), is(HttpStatus.CREATED_201));
        AccountIdResponse accountId = response.getBody();
        assertThat(accountId.getId(), not(isEmptyOrNullString()));
        assertTrue(response.getHeaders().containsKey("Location"));
        String location = response.getFirstHeader("Location");
        assertThat(location, containsString(accountId.getId()));
    }

    @Test
    public void shouldRejectAccountWithNegativeBalance() {
        // Given
        NewAccountRequest newAccountRequest = new NewAccountRequest(BigDecimal.ONE.negate());

        // When
        ApiResponse<AccountIdResponse> response = httpClient.post(ACCOUNTS_PATH, newAccountRequest, AccountIdResponse.class);

        // Then
        assertThat(response.getStatus(), is(HttpStatus.BAD_REQUEST_400));
    }

    @Test
    public void shouldRejectAccountWithoutBalance() {
        // Given
        NewAccountRequest newAccountRequest = new NewAccountRequest();

        // When
        ApiResponse<AccountIdResponse> response = httpClient.post(ACCOUNTS_PATH, newAccountRequest, AccountIdResponse.class);

        // Then
        assertThat(response.getStatus(), is(HttpStatus.BAD_REQUEST_400));
    }

    @Test
    public void shouldReturnAccountInformationIfAccountCreated() {
        // Given
        NewAccountRequest newAccountRequest = new NewAccountRequest(BigDecimal.ONE);

        // When
        ApiResponse<AccountIdResponse> response = httpClient.post(ACCOUNTS_PATH, newAccountRequest, AccountIdResponse.class);
        String accountLocation = response.getFirstHeader("Location");
        ApiResponse<AccountResponse> accountResponse = httpClient.get(accountLocation, AccountResponse.class);

        // Then
        assertThat(accountResponse.getStatus(), is(HttpStatus.OK_200));
        AccountResponse account = accountResponse.getBody();
        assertThat(account.getId(), not(isEmptyOrNullString()));
        assertThat(account.getBalance(), equalTo(newAccountRequest.getInitialBalance()));
    }

    @Test
    public void shouldReplyNotFoundIfAccountNotExists() {
        // Given
        String accountId = "someFakeId";

        // When
        ApiResponse<AccountResponse> response = httpClient.get(String.format("%s/%s", ACCOUNTS_PATH, accountId), AccountResponse.class);

        // Then
        assertThat(response.getStatus(), is(HttpStatus.NOT_FOUND_404));
    }
}
