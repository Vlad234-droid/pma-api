#!/bin/sh

sops --decrypt \
  --output ./pma-api/secrets-pte.dec.yaml \
  ./pma-api/secrets-pte.yaml
