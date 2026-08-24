# Always prefer the bundled JRE when present, ignoring any system JAVA_HOME.
# This guarantees the JVM version Quarkdown was built and tested against.
#
# Node module resolution, browser detection and the Chrome sandbox default used to live here.
# They are now resolved by Quarkdown itself, so that they behave identically whether Quarkdown
# is launched through this script or as a native binary, which has no wrapper script.
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
BUNDLED_RUNTIME="$SCRIPT_DIR/../runtime"
if [ -d "$BUNDLED_RUNTIME" ] && [ -x "$BUNDLED_RUNTIME/bin/java" ]; then
  # Canonicalize so JAVA_HOME passes the Gradle start script's validation, which rejects relative paths.
  JAVA_HOME="$(cd "$BUNDLED_RUNTIME" && pwd)"
  export JAVA_HOME
fi
