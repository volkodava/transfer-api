#!/bin/bash

export SOURCE=$(curl -X POST "http://localhost:8080/accounts" -H  "accept: application/json" -H  "Content-Type: application/json" -d "{\"initialBalance\":10000000000000}" | jq -r '.id')
export TARGET=$(curl -X POST "http://localhost:8080/accounts" -H  "accept: application/json" -H  "Content-Type: application/json" -d "{\"initialBalance\":10000000000000}" | jq -r '.id')

curl -X GET "http://localhost:8080/accounts/${SOURCE}" -H "accept: application/json" | jq .
curl -X GET "http://localhost:8080/accounts/${TARGET}" -H "accept: application/json" | jq .

echo "export SOURCE=${SOURCE} && export TARGET=${TARGET}"

#seq 1 2000 | xargs -n1 -P2000 curl -X POST "http://localhost:8080/transfers" -H "accept: application/json" -H "Content-Type: application/json" -d "{\"sourceAccountId\":\"${SOURCE}\",\"targetAccountId\":\"${TARGET}\",\"amount\":1}"
#seq 1 2000 | xargs -n1 -P2000 curl -X POST "http://localhost:8080/transfers" -H "accept: application/json" -H "Content-Type: application/json" -d "{\"sourceAccountId\":\"${TARGET}\",\"targetAccountId\":\"${SOURCE}\",\"amount\":1}"
