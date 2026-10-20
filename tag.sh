#!/usr/bin/env bash
set -euo pipefail

usage() {
    cat <<'EOF'
Usage: tag.sh <version>
       tag.sh -h | --help

Creates a git tag named <version> on the current commit and pushes it to origin.
Pushing the tag is what releases a JetBrains-internal version: the TeamCity
`markdown: publish` build runs on the tag and takes the library version from its name.

Arguments:
  <version>   A.B.C.D, digits only (e.g. 0.7.18.1).
              A.B.C versions are reserved for Maven Central and are not accepted here.

The tag is created only if ALL of these hold; otherwise the script explains the
problem and how to fix it, and creates nothing:
  1. <version> matches A.B.C.D
  2. the current branch is `master`
  3. local `master` is identical to `origin/master` (same latest commit)
  4. the tag does not exist yet, locally or on origin (versions are immutable)

The script never switches branches, pulls, rebases or pushes branches.
It only runs `git fetch origin master` (updates the `origin/master` ref),
then `git tag` and `git push` for the tag itself.

Examples:
  ./tag.sh 0.7.18.1      # tag the latest master as 0.7.18.1 and push the tag
  ./tag.sh -h            # show this help
EOF
}

fail() {
    printf 'error: %s\nfix:   %s\n' "$1" "$2" >&2
    exit 1
}

if [[ ${1:-} == -h || ${1:-} == --help ]]; then
    usage
    exit 0
fi
if [[ $# -ne 1 ]]; then
    usage >&2
    exit 1
fi
version=$1

if [[ ! $version =~ ^[0-9]+\.[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
    fail "version '$version' does not match A.B.C.D (digits only, four parts)." \
         "use e.g. 0.7.18.1. A.B.C versions are released to Maven Central, not by tags."
fi

branch=$(git branch --show-current)
if [[ $branch != master ]]; then
    fail "current branch is '${branch:-detached HEAD}', tags can only be created from 'master'." \
         "run 'git switch master', then re-run."
fi

echo "Fetching origin/master..."
git fetch origin master --quiet

read -r ahead behind < <(git rev-list --left-right --count HEAD...origin/master)
if [[ $ahead -gt 0 && $behind -gt 0 ]]; then
    fail "local master has diverged from origin/master ($ahead local commit(s) not on origin, $behind origin commit(s) not local)." \
         "reconcile it first, e.g. 'git pull --rebase' and push, then re-run."
elif [[ $behind -gt 0 ]]; then
    fail "local master is $behind commit(s) behind origin/master, so it is not the latest commit." \
         "run 'git pull --ff-only', then re-run."
elif [[ $ahead -gt 0 ]]; then
    fail "local master has $ahead commit(s) that are not on origin/master; the tag would point to a commit the remote doesn't have." \
         "push or merge them first (the tag must be on the latest origin/master), then re-run."
fi

if git rev-parse -q --verify "refs/tags/$version" >/dev/null; then
    fail "tag '$version' already exists locally." \
         "versions are immutable: pick the next version, or delete the stray local tag with 'git tag -d $version' if it was never pushed."
fi
remote_tag=$(git ls-remote --tags origin "refs/tags/$version")
if [[ -n $remote_tag ]]; then
    fail "tag '$version' already exists on origin." \
         "versions are immutable and can't be re-published: pick the next version."
fi

git tag "$version"
git push origin "refs/tags/$version"
echo "Tag $version created and pushed to origin."
