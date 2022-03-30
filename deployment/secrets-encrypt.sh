#!/bin/sh

# Login to TescoAzure tenant
# az login --tenant f55b1f7d-7a7f-49e4-9b90-55218aad89f8

if [ -z "$1" ]
then
  echo "Usage: "
  echo "  secrets-encrypt.sh <env_name>"
  echo "  where <env_name>: one of dev, ppe, pte, prod"
  exit -1
fi

env_name=`echo "$1" | tr '[:upper:]' '[:lower:]'`
env_dir="`dirname "$0"`/pma-api"
env_file_part="secrets-$env_name"

case $env_name in
  dev)
    azure_kv='https://euw-dev-214-pma-kv.vault.azure.net/keys/euw-dev-214-pma-sops/b24f7c8fb15547d79040a1dd1dc475d0'
    ;;
  ppe)
    azure_kv='https://euw-ppe-214-pma-kv.vault.azure.net/keys/euw-ppe-214-pma-sops/73f39302a1ea404f8e665104c86f287b'
    ;;
  pte)
    azure_kv='https://euw-pte-214-pma-gl.vault.azure.net/keys/euw-pte-214-pma-sops/0d19b7e8c77c4657a8192f49f29997e5'
    ;;
  prod)
    azure_kv='https://euw-prod-214-pma-kv.vault.azure.net/keys/euw-prod-214-pma-sops/b0b9c7fe32eb4558b401eec61c8ab3c3'
    ;;
  *)
    echo "Environment '$env_name' is not supported for encrypting sercets"
    exit -2
    ;;
esac

if [ -f "$env_dir/$env_file_part.dec.yaml" ] 
then
  sops --encrypt --azure-kv $azure_kv --output $env_dir/$env_file_part.yaml $env_dir/$env_file_part.dec.yaml
else
  echo "File $env_dir/$env_file_part.dec.yaml doesn't exists" 
fi
