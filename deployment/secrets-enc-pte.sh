#!/bin/sh

sops --encrypt \
  --azure-kv https://euw-pte-214-pma-gl.vault.azure.net/keys/euw-pte-214-pma-sops/0d19b7e8c77c4657a8192f49f29997e5 \
  --output ./pma-api/secrets-pte.yaml \
  ./pma-api/secrets-pte.dec.yaml
