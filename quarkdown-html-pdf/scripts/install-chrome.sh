#!/bin/bash

# Installs chrome-headless-shell, the headless browser Quarkdown requires for PDF export,
# at the version pinned by the chrome.properties file shipped with this distribution,
# verifying the downloaded archive against its pinned SHA-256 checksum.
#
# Usage: install-chrome.sh <target-dir>
#
# The browser is extracted to <target-dir>/chrome-headless-shell-<platform>/,
# and the absolute path of its executable is printed to stdout (status messages go to stderr),
# to be assigned to the QD_CHROME_PATH environment variable or the --chrome-path option.

set -e

TARGET_DIR="${1:?Usage: install-chrome.sh <target-dir>}"

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROPERTIES_FILE="$SCRIPT_DIR/chrome.properties"

if [[ ! -f "$PROPERTIES_FILE" ]]; then
  echo "Error: browser properties file not found at $PROPERTIES_FILE" >&2
  exit 1
fi

# Reads a `key=value` property from the properties file.
property() {
  grep "^$1=" "$PROPERTIES_FILE" | head -1 | cut -d= -f2 | tr -d '[:space:]'
}

CHROME_VERSION="$(property version)"
if [[ -z "$CHROME_VERSION" ]]; then
  echo "Error: no version pinned in $PROPERTIES_FILE" >&2
  exit 1
fi

# Chrome for Testing platform identifier.
case "$(uname -s)-$(uname -m)" in
  Linux-x86_64|Linux-amd64)    PLATFORM="linux64" ;;
  Darwin-arm64|Darwin-aarch64) PLATFORM="mac-arm64" ;;
  Darwin-x86_64)               PLATFORM="mac-x64" ;;
  *) echo "Error: unsupported platform: $(uname -s) $(uname -m)" >&2; exit 1 ;;
esac

EXPECTED_SHA="$(property "$PLATFORM")"
if [[ -z "$EXPECTED_SHA" ]]; then
  echo "Error: no SHA-256 checksum pinned for $PLATFORM in $PROPERTIES_FILE" >&2
  exit 1
fi

echo "Installing chrome-headless-shell $CHROME_VERSION (required for PDF export)..." >&2

TMP_ZIP="$(mktemp).zip"
trap 'rm -f "$TMP_ZIP"' EXIT

curl -fL --show-error --silent \
  "https://storage.googleapis.com/chrome-for-testing-public/$CHROME_VERSION/$PLATFORM/chrome-headless-shell-$PLATFORM.zip" \
  -o "$TMP_ZIP"

# Verify the archive against the pinned checksum before extracting it.
if command -v shasum >/dev/null; then
  ACTUAL_SHA="$(shasum -a 256 "$TMP_ZIP" | cut -d' ' -f1)"
else
  ACTUAL_SHA="$(sha256sum "$TMP_ZIP" | cut -d' ' -f1)"
fi
if [[ "$ACTUAL_SHA" != "$EXPECTED_SHA" ]]; then
  echo "Error: SHA-256 mismatch for the downloaded browser archive." >&2
  echo "  expected: $EXPECTED_SHA" >&2
  echo "  actual:   $ACTUAL_SHA" >&2
  exit 1
fi

mkdir -p "$TARGET_DIR"
unzip -q -o "$TMP_ZIP" -d "$TARGET_DIR"

BROWSER_PATH="$TARGET_DIR/chrome-headless-shell-$PLATFORM/chrome-headless-shell"
if [[ ! -x "$BROWSER_PATH" ]]; then
  echo "Error: browser executable not found at $BROWSER_PATH after extraction" >&2
  exit 1
fi

echo "$BROWSER_PATH"
