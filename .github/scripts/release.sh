#!/usr/bin/env bash
# Starts a release via the Release workflow.
# Usage: .github/scripts/release.sh <version>    e.g. .github/scripts/release.sh 2.8.0 (or v2.8.0)

set -euo pipefail

gh workflow run release.yml -f version="${1:?usage: $0 <version>}"
