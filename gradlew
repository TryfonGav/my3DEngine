#!/usr/bin/env sh
# Gradle wrapper script (lightweight standard template)
set -e
DIR="$(cd "$(dirname "$0")" && pwd)"
if [ -z "$JAVA_HOME" ]; then
  echo "JAVA_HOME is not set. Please set JAVA_HOME to your JDK installation." >&2
fi
JAVA_CMD="${JAVA_HOME:+$JAVA_HOME/bin/}java"
if [ ! -x "$JAVA_CMD" ]; then
  JAVA_CMD=java
fi
exec "$JAVA_CMD" -classpath "$DIR/gradle/wrapper/gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain "$@"
