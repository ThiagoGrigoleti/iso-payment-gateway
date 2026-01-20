#!/bin/sh
set -e
MAVEN_PROJECTBASEDIR="${MAVEN_BASEDIR:-$(cd "$(dirname "$0")" && pwd)}"
MAVEN_WRAPPER_JAR="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar"
MAVEN_WRAPPER_PROPERTIES="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.properties"

if [ ! -f "$MAVEN_WRAPPER_JAR" ]; then
    if [ -f "$MAVEN_WRAPPER_PROPERTIES" ]; then
        wrapperUrl=$(grep 'wrapperUrl' "$MAVEN_WRAPPER_PROPERTIES" | cut -d'=' -f2-)
        if command -v wget > /dev/null; then
            wget -q -O "$MAVEN_WRAPPER_JAR" "$wrapperUrl"
        elif command -v curl > /dev/null; then
            curl -s -o "$MAVEN_WRAPPER_JAR" "$wrapperUrl"
        fi
    fi
fi

if [ -f "$MAVEN_WRAPPER_JAR" ]; then
    exec java -jar "$MAVEN_WRAPPER_JAR" "$@"
else
    exec mvn "$@"
fi
