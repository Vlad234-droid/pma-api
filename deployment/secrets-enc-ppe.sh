#!/bin/sh

sops --encrypt \
  --azure-kv https://euw-ppe-214-pma-kv.vault.azure.net/keys/euw-ppe-214-pma-sops/73f39302a1ea404f8e665104c86f287b \
  --output ./pma-api/secrets-ppe.yaml \
  ./pma-api/secrets-ppe.dec.yaml
