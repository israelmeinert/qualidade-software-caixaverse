#!/usr/bin/env bash
set -euo pipefail
umask 077

image=${1:?Imagem por digest}
: "${DEPLOY_HOST:?Variable DEPLOY_HOST pendente}"
: "${DEPLOY_USER:?Variable DEPLOY_USER pendente}"
: "${DEPLOY_DIR:?Variable DEPLOY_DIR pendente}"
: "${DEPLOY_SMOKE_URL:?Variable DEPLOY_SMOKE_URL pendente}"
: "${DEPLOY_SSH_KEY:?Secret DEPLOY_SSH_KEY pendente}"
: "${DEPLOY_KNOWN_HOSTS:?Secret DEPLOY_KNOWN_HOSTS pendente}"

port=${DEPLOY_PORT:-22}
[[ $DEPLOY_HOST =~ ^[a-zA-Z0-9][a-zA-Z0-9.-]*$ && $DEPLOY_USER =~ ^[a-z_][a-z0-9_-]*$ &&
   $port =~ ^[1-9][0-9]{0,4}$ && $port -le 65535 && $DEPLOY_DIR =~ ^/[a-zA-Z0-9_/-]+$ &&
   $image =~ ^ghcr\.io/[a-z0-9_.-]+/[a-z0-9_.-]+@sha256:[a-f0-9]{64}$ &&
   $DEPLOY_SMOKE_URL =~ ^https?://[a-zA-Z0-9.:-]+/q/health/ready$ ]] || {
  echo 'Parâmetros SSH ou de implantação inválidos.' >&2
  exit 2
}

credentials=$(mktemp -d)
trap 'rm -f -- "$credentials/key" "$credentials/known_hosts"; rmdir -- "$credentials"' EXIT
printf '%s\n' "$DEPLOY_SSH_KEY" > "$credentials/key"
printf '%s\n' "$DEPLOY_KNOWN_HOSTS" > "$credentials/known_hosts"

ssh -T -p "$port" -i "$credentials/key" -o IdentitiesOnly=yes -o BatchMode=yes \
  -o StrictHostKeyChecking=yes -o GlobalKnownHostsFile=/dev/null \
  -o "UserKnownHostsFile=$credentials/known_hosts" -o ConnectTimeout=10 \
  -- "$DEPLOY_USER@$DEPLOY_HOST" "bash -s -- '$DEPLOY_DIR' '$image' '$DEPLOY_SMOKE_URL'" \
  < scripts/deploy/update.sh
