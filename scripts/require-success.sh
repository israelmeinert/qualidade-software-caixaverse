#!/usr/bin/env bash
set -euo pipefail

: "${REQUIRED_RESULTS:?Resultados dos jobs obrigatórios ausentes}"
printf '%s\n' "$REQUIRED_RESULTS" | jq -r 'to_entries[] | "\(.key): \(.value.result)"'
printf '%s\n' "$REQUIRED_RESULTS" | jq -e '
  type == "object" and length > 0 and all(.[]; .result == "success")
' >/dev/null
