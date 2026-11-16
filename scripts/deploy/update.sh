#!/usr/bin/env bash
set -euo pipefail
umask 077

directory=${1:?Diretório de implantação}
image=${2:?Imagem por digest}
smoke_url=${3:?URL do smoke test}
image_pattern='^ghcr\.io/[a-z0-9_.-]+/[a-z0-9_.-]+@sha256:[a-f0-9]{64}$'
[[ $directory =~ ^/[a-zA-Z0-9_/-]+$ && $image =~ $image_pattern &&
   $smoke_url =~ ^https?://[a-zA-Z0-9.:-]+/q/health/ready$ ]] || {
  echo 'Parâmetros de deploy inválidos.' >&2
  exit 2
}

cd -- "$directory"
[[ -r .env && -r compose.deploy.yaml && -r jwt-public.pem ]]
exec 9>.deploy.lock
flock -w 30 9
previous=
if [[ -f .current-image ]]; then
  read -r previous < .current-image
  [[ $previous =~ $image_pattern ]]
fi

compose() {
  REWARDS_IMAGE=$1 docker compose -f compose.deploy.yaml "${@:2}"
}

smoke() {
  curl --fail --silent --show-error --max-time 8 --retry 4 --retry-all-errors \
    --output /dev/null "$smoke_url"
}

rollback() {
  echo 'Verificação falhou. Iniciando reversão da aplicação.' >&2
  if [[ -n $previous ]]; then
    compose "$previous" up -d --no-deps --wait --wait-timeout 120 app && smoke
  else
    compose "$image" stop app
  fi
}

compose "$image" pull app
if compose "$image" up -d --wait --wait-timeout 120 && smoke; then
  if [[ -n $previous && $previous != "$image" ]]; then
    printf '%s\n' "$previous" > .previous-image.tmp
    mv -f -- .previous-image.tmp .previous-image
  fi
  printf '%s\n' "$image" > .current-image.tmp
  mv -f -- .current-image.tmp .current-image
  echo 'Imagem em execução e readiness verificado.'
else
  rollback || echo 'A reversão também falhou. Verifique a VM e as migrations.' >&2
  exit 1
fi
