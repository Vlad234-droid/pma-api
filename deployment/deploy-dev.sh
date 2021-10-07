#!/bin/sh

helm secrets upgrade --install pma-api-service ./pma-api/ -f ./pma-api/values-dev.yaml -f ./pma-api/secrets-dev.yaml --set image.tag=$1 -n api
