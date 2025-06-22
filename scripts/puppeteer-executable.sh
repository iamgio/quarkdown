if [ -z "$PUPPETEER_EXECUTABLE_PATH" ]; then
  ERROR_MESSAGE="[!] Could not find a suitable installation of Google Chrome, Chromium, or Firefox. PDF generation may not be available."
  if [ "$(uname)" = "Darwin" ]; then
    # macOS
    if [ -x "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome" ]; then
      export PUPPETEER_EXECUTABLE_PATH="/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"
    elif [ -x "/Applications/Chromium.app/Contents/MacOS/Chromium" ]; then
      export PUPPETEER_EXECUTABLE_PATH="/Applications/Chromium.app/Contents/MacOS/Chromium"
    elif [ -x "/Applications/Firefox.app/Contents/MacOS/firefox" ]; then
      export PUPPETEER_EXECUTABLE_PATH="/Applications/Firefox.app/Contents/MacOS/firefox"
      export PUPPETEER_BROWSER="firefox"
    else
      echo "$ERROR_MESSAGE"
    fi
  else
    # Linux
    if command -v google-chrome > /dev/null; then
      export PUPPETEER_EXECUTABLE_PATH="$(command -v google-chrome)"
    elif command -v chromium-browser > /dev/null; then
      export PUPPETEER_EXECUTABLE_PATH="$(command -v chromium-browser)"
    elif command -v chromium > /dev/null; then
      export PUPPETEER_EXECUTABLE_PATH="$(command -v chromium)"
    elif command -v firefox > /dev/null; then
      export PUPPETEER_EXECUTABLE_PATH="$(command -v firefox)"
      export PUPPETEER_BROWSER="firefox"
    else
      echo "$ERROR_MESSAGE"
    fi
  fi
fi