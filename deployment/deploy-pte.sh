#!/bin/sh

kubectl config use-context euw-pte-214-pma-aks-admin
helm secrets upgrade \
  --install pma-api-service ./pma-api/ \
  --set image.tag=$1 \
  -f ./pma-api/values-pte.yaml \
  -f ./pma-api/secrets-pte.yaml \
  -n api
