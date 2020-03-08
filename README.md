# transfer-api
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

Transfer processing consists of the following steps:
- register transfer (single thread, thread-1)
- validate transfer (single thread, thread-1)
- withdraw money from the source account (single thread, thread-1)
- (partition into blocks of threads)
    - deposit money to the target account (single thread, thread-N)
    - finalize transfer, check transfer state and update accordingly (single thread, thread-N)
    - complete transfer, save transfer to in-memory storage (single thread, thread-N)

Java `ConcurrentHashMap` used as in-memory storage. Withdraw and deposit operations on accounts performed atomically.

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
