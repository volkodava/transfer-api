# transfer-api
![](https://github.com/volkodava/transfer-api/workflows/transfer-api/badge.svg)

A simple money transfer REST API implementation with concurrency

## Features

### Account API

- Create account
- View account details

### Transfer API

- Create transfer
- View transfer details
- List all transfers

## Notes

Solution done with assumption that we deal only with debit accounts.

The API designed as a REST APIs with JSON payload.

Transfer processing consists of the following steps (see picture below):
- register transfer (single thread, thread-1)
- validate transfer (single thread, thread-1)
- withdraw money from the source account (single thread, thread-1)
- (partition into blocks of threads)
    - deposit money to the target account (single thread, thread-N)
    - finalize transfer, check transfer state and update accordingly (single thread, thread-N)
    - complete transfer, save transfer to in-memory storage (single thread, thread-N)

<img src="./docs/pipeline.png" alt="" style="width:600px;">

All these steps implemented as pipeline using `rxjava` library (see: [PipelineExecutor.java#L69](./src/main/java/com/demo/api/transfer/manager/PipelineExecutor.java#L69)). 

Java `ConcurrentHashMap` used as in-memory storage. Withdraw and deposit operations on accounts performed atomically. 

Throttling of transfer requests implemented using Java `BlockingQueue`, 
which is also used as communication channel between service (accept transfer requests) and pipeline executor (process transfer requests). 

Mission critical pieces of applications covered with tests.

## Tools, Frameworks, Libraries

- Java 12+
- [Maven](https://maven.apache.org/) build-automation tool
- [javalin](https://javalin.io/) lightweight web framework to create REST APIs
- [guice](https://github.com/google/guice) lightweight dependency injection framework
- [rxjava](https://github.com/ReactiveX/RxJava) library for asynchronous programming
- [Swagger UI](https://swagger.io/tools/swagger-ui/) library for visual documentation

## How to run the demo

- Clone the project from GitHub: `git clone git@github.com:volkodava/transfer-api.git`

- Build the project: `mvn clean install`

- Run application: `java -jar target/transfer-api-1.0.jar`
    - Configuration options can be overridden using environment variables: `PORT=8080 BUFFER_SIZE=1000 MAX_THREADS=100 java -jar target/transfer-api-1.0.jar`

- Execute unit tests: `mvn test`

- Execute unit + integration tests: `mvn integration-test`

## Examples

- To create account with initial balance of 1000 EUR:

```bash
curl -X POST "http://localhost:8080/accounts" -H  "accept: application/json" -H  "Content-Type: application/json" -d "{\"initialBalance\":1000}"
```
account `id` will be returned in the JSON payload, `Location` header will contain link to account information API.

- To request account information:

```bash
curl -X GET "http://localhost:8080/accounts/de07e939-55dd-4086-b559-86db399e51d5" -H  "accept: application/json"
```

- To transfer 10 EUR from account `de07e939-55dd-4086-b559-86db399e51d5` to account `8810b77a-f326-4e27-8e48-4e77a7f27e05`:

```bash
curl -X POST "http://localhost:8080/transfers" -H  "accept: application/json" -H  "Content-Type: application/json" -d "{\"sourceAccountId\":\"de07e939-55dd-4086-b559-86db399e51d5\",\"targetAccountId\":\"8810b77a-f326-4e27-8e48-4e77a7f27e05\",\"amount\":10}"
```
account `id` will be returned in the JSON payload, `Location` header will contain link to transfer information API.

- To request transfer information:

```bash
curl -X GET "http://localhost:8080/transfers/3d98e966-2a39-46e2-9afc-d2b7cf2285d4" -H  "accept: application/json"
```

## CI/CD builds

CI/CD Builds: https://github.com/volkodava/transfer-api/actions

## API Documentation

API documentation available after application starts at `http://localhost:${PORT}/swagger-ui`.

<img src="./docs/swagger.png" alt="" style="width:500px;">

## Project Structure

TBD
