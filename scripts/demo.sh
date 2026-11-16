#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

case ${1:-help} in
  up)
    docker compose up -d --wait postgres
    ./scripts/dev-keys.sh
    exec ./mvnw quarkus:dev
    ;;
  verify)
    docker compose up -d --wait postgres
    exec ./mvnw -B -Pcoverage clean verify
    ;;
  pit)
    exec ./mvnw -B -Pmutation test-compile org.pitest:pitest-maven:1.30.2:mutationCoverage
    ;;
  *)
    echo 'Uso: ./scripts/demo.sh up | verify | pit'
    ;;
esac
