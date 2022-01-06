#!/bin/sh

kubectl config use-context euw-prod-214-pma-aks-admin
helm secrets upgrade \
  --install pma-api-service ./pma-api/ \
  --set image.tag=$1 \
  -f ./pma-api/values-prod.yaml \
  -f ./pma-api/secrets-prod.yaml \
  -f ./pma-api/prod/* \
  -n api
