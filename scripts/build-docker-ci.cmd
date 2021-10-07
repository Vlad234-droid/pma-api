@echo off

setlocal

rem set project root as current dir
cd /D "%~dp0.."

rem set image tag
if "%1"=="" (
    echo Default image tag will be used: "pma_api:latest"
    set IMAGE_TAG=pma_api:latest
) else (
    set IMAGE_TAG=%1
)

docker build ^
  --progress plain ^
  --tag %IMAGE_TAG% ^
  --build-arg BUILD_PROFILES=default,docker ^
  --file dockerfiles/pma-api_docker_multistage_build.Dockerfile ^
  .

endlocal
