#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
[[ -s .local/private.pem ]] || { echo 'Execute scripts/dev-keys.sh primeiro.' >&2; exit 2; }
exec java scripts/DevJwt.java token "${1:-ana}" "${2:-employee}"
