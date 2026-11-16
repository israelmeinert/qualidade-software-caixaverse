#!/usr/bin/env bash
set -euo pipefail

run_id=${1:?ID de execução da entrega}
repository=${GITHUB_REPOSITORY:?Repositório ausente}
[[ $run_id =~ ^[1-9][0-9]{0,19}$ ]]
api="repos/$repository/actions"
workflow_id=$(gh api "$api/workflows/quality.yml" --jq '.id')
run=$(gh api "$api/runs/$run_id")

# Garanta que o servidor associa a execução ao workflow e à branch confiáveis.
jq -e --arg repo "$repository" --argjson workflow "$workflow_id" '
  .workflow_id == $workflow and .path == ".github/workflows/quality.yml"
  and .repository.full_name == $repo and .head_repository.full_name == $repo
  and .event == "push" and .head_branch == "final"
  and .status == "completed" and .conclusion == "success"
' <<<"$run" >/dev/null || { echo 'A execução não é uma entrega aprovada de final.' >&2; exit 1; }

sha=$(jq -er '.head_sha | select(test("^[a-f0-9]{40}$"))' <<<"$run")
attempt=$(jq -er '.run_attempt | select(type == "number" and . >= 1)' <<<"$run")
git merge-base --is-ancestor "$sha" HEAD || {
  echo 'O SHA da entrega não pertence ao histórico da revisão final do dispatch.' >&2
  exit 1
}

jobs=$(gh api "$api/runs/$run_id/attempts/$attempt/jobs?per_page=100")
jq -e '
  .jobs as $jobs
  | ["tests", "mutation", "sonar", "quality-required-delivery", "publish-image", "deploy-staging"]
  | all(.[]; . as $name | [$jobs[] | select(.name == $name)] as $matched
      | ($matched | length == 1) and ($matched[0].conclusion == "success"))
' <<<"$jobs" >/dev/null || { echo 'Falta aprovação de job obrigatório ou de staging no mesmo run.' >&2; exit 1; }

artifact_name="release-$run_id-$attempt"
artifact=$(gh api "$api/runs/$run_id/artifacts?name=$artifact_name")
artifact_id=$(jq -er --arg name "$artifact_name" --arg sha "$sha" --argjson run "$run_id" '
  [.artifacts[] | select(.name == $name and .expired == false
    and .workflow_run.id == $run and .workflow_run.head_sha == $sha
    and .workflow_run.head_branch == "final")] | select(length == 1) | .[0].id
' <<<"$artifact")
temp=$(mktemp -d)
trap 'rm -f -- "$temp/release.zip"; rmdir -- "$temp"' EXIT
gh api "$api/artifacts/$artifact_id/zip" > "$temp/release.zip"

python3 - "$temp/release.zip" "$repository" "$sha" "$run_id" "$attempt" <<'PY'
import json
import re
import sys
import zipfile

archive, repository, sha, run_id, attempt = sys.argv[1:]
with zipfile.ZipFile(archive) as release:
    if release.namelist() != ["release.json"] or release.getinfo("release.json").file_size > 4096:
        sys.exit("Artefato de release inválido.")
    manifest = json.loads(release.read("release.json"))
image = manifest.get("image", "")
image_name = re.escape("ghcr.io/" + repository.lower())
if (manifest.get("repository"), manifest.get("sha"), manifest.get("run_id"), manifest.get("attempt")) != (
    repository, sha, run_id, attempt
) or not re.fullmatch(image_name + r"@sha256:[a-f0-9]{64}", image):
    sys.exit("O manifesto não corresponde ao repositório, commit e execução aprovados.")
print(image)
PY
