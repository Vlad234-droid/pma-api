#!/bin/sh

cd `dirname $0`/..

IMAGE_TAG=$1

if [ "$IMAGE_TAG" = "" ]; then
  echo Default image tag will be used: "pma_api:latest"
  IMAGE_TAG=pma_api:latest
fi

docker build \
  --progress plain \
  --tag $IMAGE_TAG \
  --build-arg BUILD_ENV=dev \
  --file dockerfiles/pma-api_docker_multistage_build.Dockerfile \
  .;

