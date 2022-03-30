#!/bin/sh

# Login to TescoAzure tenant
# az login --tenant f55b1f7d-7a7f-49e4-9b90-55218aad89f8

if [ -z "$1" ]
then
  echo "Usage: "
  echo "  secrets-decrypt.sh <env_name>"
  echo "  where <env_name>: one of dev, ppe, pte, prod"
  exit 1
fi

env_name=`echo "$1" | tr '[:upper:]' '[:lower:]'`
env_dir="`dirname "$0"`/pma-api"
env_file_part="secrets-$env_name"

if [ -f "$env_dir/$env_file_part.yaml" ] 
then
  sops --decrypt --output $env_dir/$env_file_part.dec.yaml $env_dir/$env_file_part.yaml
else
  echo "File $env_dir/$env_file_part.yaml doesn't exists" 
fi
