#!/bin/sh

kubectl config use-context euw-dev-214-pma-aks-admin
helm secrets upgrade \
  --install pma-api-service ./pma-api/ \
  --set image.tag=$1 \
  -f ./pma-api/values-dev.yaml \
  -f ./pma-api/secrets-dev.yaml \
  -f ./pma-api/dev/* \
  -n api
