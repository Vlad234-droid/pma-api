#!/bin/sh

sops --encrypt \
  --azure-kv https://euw-prod-214-pma-kv.vault.azure.net/keys/euw-prod-214-pma-sops/b0b9c7fe32eb4558b401eec61c8ab3c3 \
  --output ./pma-api/secrets-prod.yaml \
  ./pma-api/secrets-prod.dec.yaml
