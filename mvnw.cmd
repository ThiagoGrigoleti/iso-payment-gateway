@echo off
setlocal

set MAVEN_PROJECTBASEDIR=%~dp0
set MAVEN_WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.jar
set MAVEN_WRAPPER_PROPERTIES=%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.properties

if not exist "%MAVEN_WRAPPER_JAR%" (
    if exist "%MAVEN_WRAPPER_PROPERTIES%" (
        for /f "tokens=2 delims==" %%a in ('findstr "wrapperUrl" "%MAVEN_WRAPPER_PROPERTIES%"') do set wrapperUrl=%%a
        powershell -Command "Invoke-WebRequest -Uri '%wrapperUrl%' -OutFile '%MAVEN_WRAPPER_JAR%'"
    )
)

if exist "%MAVEN_WRAPPER_JAR%" (
    java -jar "%MAVEN_WRAPPER_JAR%" %*
) else (
    mvn %*
)

endlocal
