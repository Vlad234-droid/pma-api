#!/bin/sh

kubectl config use-context euw-ppe-214-pma-aks-admin
helm secrets upgrade --install pma-api-service ./pma-api/ -f ./pma-api/values-ppe.yaml -f ./pma-api/secrets-ppe.yaml -f ./pma-api/ppe/* --set image.tag=$1 -n api
