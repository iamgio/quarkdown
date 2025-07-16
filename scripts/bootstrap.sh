#!/bin/bash

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