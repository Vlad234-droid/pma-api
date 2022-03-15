#!/bin/sh

sops --encrypt \
  --azure-kv https://euw-dev-214-pma-kv.vault.azure.net/keys/euw-dev-214-pma-sops/b24f7c8fb15547d79040a1dd1dc475d0 \
  --output ./pma-api/secrets-dev.yaml \
  ./pma-api/secrets-dev.dec.yaml
