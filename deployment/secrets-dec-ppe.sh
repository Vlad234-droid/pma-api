#!/bin/sh

sops --decrypt \
  --output ./pma-api/secrets-ppe.dec.yaml \
  ./pma-api/secrets-ppe.yaml
