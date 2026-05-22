@echo off
SET DIR=%~dp0
if "%JAVA_HOME%"=="" (
  echo JAVA_HOME is not set. Please set JAVA_HOME to your JDK installation.
)
set JAVA_CMD=%JAVA_HOME%\bin\java.exe
if not exist "%JAVA_CMD%" (
  set JAVA_CMD=java
)
"%JAVA_CMD%" -classpath "%DIR%\gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*
