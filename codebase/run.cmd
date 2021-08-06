@echo off
rem ----------------------------
rem Bootstrap Script for Windows
rem ----------------------------

@if not "%ECHO%" == ""  echo %ECHO%
@if "%OS%" == "Windows_NT" setlocal

if "%OS%" == "Windows_NT" (
  set "DIRNAME=%~dp0%"
) else (
  set DIRNAME=.\
)

pushd %DIRNAME%..
if "x%SERVICE_HOME%" == "x" (
  set "SERVICE_HOME=%CD%"
)
popd

set DIRNAME=

if "%OS%" == "Windows_NT" (
  set "PROGNAME=%~nx0%"
) else (
  set "PROGNAME=run.cmd"
)

if "x%JAVA_OPTS%" == "x" (
  set "JAVA_OPTS=-Dprogram.name=%PROGNAME%"
) else (
  set "JAVA_OPTS=-Dprogram.name=%PROGNAME% %JAVA_OPTS%"
)

if "x%JAVA_HOME%" == "x" (
  set  JAVA=java
  echo JAVA_HOME is not set. Unexpected results may occur.
  echo Set JAVA_HOME to the directory of your local JDK to avoid this message.
) else (
  set "JAVA=%JAVA_HOME%\bin\java"
  if exist "%JAVA_HOME%\lib\tools.jar" (
    set "JAVAC_JAR=%JAVA_HOME%\lib\tools.jar"
  )
)

rem Add -server to the JVM options, if supported
"%JAVA%" -server -version 2>&1 | findstr /I hotspot > nul
if not errorlevel == 1 (
  set "JAVA_OPTS=%JAVA_OPTS% -server"
)

rem Set Java platform if 64-Bit JVM used
set JAVA_PLATFORM=
"%JAVA%" -version 2>&1 | findstr /I "64-Bit ^| x86_64" > nul
if not errorlevel == 1 (
  if /I "%PROCESSOR_ARCHITECTURE%"=="IA64"  (set JAVA_PLATFORM=i64
  ) else if /I "%PROCESSOR_ARCHITECTURE%"=="AMD64" (set JAVA_PLATFORM=x64
  ) else if /I "%PROCESSOR_ARCHITECTURE%"=="x64"   (set JAVA_PLATFORM=x64
  ) else if /I "%PROCESSOR_ARCHITEW6432%"=="IA64"  (set JAVA_PLATFORM=i64
  ) else if /I "%PROCESSOR_ARCHITEW6432%"=="AMD64" (set JAVA_PLATFORM=x64
  ) else (
    echo PROCESSOR_ARCHITECTURE is not set. Unexpected results may occur.
    echo Set PROCESSOR_ARCHITECTURE according to the 64-Bit JVM used.
  )
)

"%JAVA%" -Dloader.path=application/src/main/image/config -jar application/build/libs/application-1.0.0-SNAPSHOT.jar %*
