# Always prefer the bundled JRE when present, ignoring any system JAVA_HOME.
# This guarantees the JVM version Quarkdown was built and tested against.
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
BUNDLED_RUNTIME="$SCRIPT_DIR/../runtime"
if [ -d "$BUNDLED_RUNTIME" ] && [ -x "$BUNDLED_RUNTIME/bin/java" ]; then
  # Canonicalize so JAVA_HOME passes the Gradle start script's validation, which rejects relative paths.
  JAVA_HOME="$(cd "$BUNDLED_RUNTIME" && pwd)"
  export JAVA_HOME
fi

if [ -n "$QD_NPM_PREFIX" ]; then
  export NODE_PATH="$QD_NPM_PREFIX/node_modules"
fi

# Browser detection
if [ "$(uname)" = "Darwin" ]; then
  # macOS
  if [ -x "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome" ]; then
    export BROWSER_CHROME="/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"
  fi
  if [ -x "/Applications/Chromium.app/Contents/MacOS/Chromium" ]; then
    export BROWSER_CHROMIUM="/Applications/Chromium.app/Contents/MacOS/Chromium"
  fi
  if [ -x "/Applications/Firefox.app/Contents/MacOS/firefox" ]; then
    export BROWSER_FIREFOX="/Applications/Firefox.app/Contents/MacOS/firefox"
  fi
else
  # Linux
  export QD_NO_SANDBOX=true # No Chrome sandbox on Linux
  if command -v google-chrome > /dev/null; then
    export BROWSER_CHROME="$(command -v google-chrome)"
  fi
  if command -v chromium-browser > /dev/null; then
    export BROWSER_CHROMIUM="$(command -v chromium-browser)"
  fi
  if command -v chromium > /dev/null; then
    export BROWSER_CHROMIUM="$(command -v chromium)"
  fi
  if command -v firefox > /dev/null; then
    export BROWSER_FIREFOX="$(command -v firefox)"
  fi
fi