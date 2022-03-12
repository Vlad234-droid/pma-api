#!/bin/sh

sops --decrypt \
  --output ./pma-api/secrets-prod.dec.yaml \
  ./pma-api/secrets-prod.yaml
