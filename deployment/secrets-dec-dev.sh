#!/bin/sh

sops --decrypt \
  --output ./pma-api/secrets-dev.dec.yaml \
  ./pma-api/secrets-dev.yaml
