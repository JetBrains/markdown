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
  4. A.B.C equals `version` in gradle.properties of that commit
     (version=0.7.18 allows 0.7.18.1, 0.7.18.2, ... but not 0.7.19.1)
  5. the tag does not exist yet, locally or on origin (versions are immutable)

The script never switches branches, pulls, rebases or updates branches.
It only runs `git fetch origin master` (updates the `origin/master` ref), then `git tag`
and one atomic `git push` of the tag. That push also names `master`, but only as a lease:
it is rejected, publishing nothing, if `origin/master` moved after the checks above.

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

# Read from HEAD (the commit being tagged), not the working tree, which may have uncommitted edits.
project_version=$(git show HEAD:gradle.properties | sed -n 's/^version=//p')
if [[ -z $project_version ]]; then
    fail "no 'version=' line found in gradle.properties at HEAD, can't check the version prefix." \
         "restore the 'version=A.B.C' line in gradle.properties on master, then re-run."
fi
if [[ ${version%.*} != "$project_version" ]]; then
    fail "version '$version' has prefix '${version%.*}', but gradle.properties has version=$project_version." \
         "use $project_version.D (e.g. $project_version.1)"
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
# The push also names master, only as a lease on the checked commit: if master moved since the check
# above, git rejects the whole push, so no tag is published from an outdated commit.
# --no-follow-tags: with push.followTags=true git would also push other local annotated tags.
if ! git push --atomic --no-follow-tags --force-with-lease="refs/heads/master:$(git rev-parse HEAD)" \
        origin HEAD:refs/heads/master "refs/tags/$version"; then
    git tag -d "$version" >/dev/null
    fail "push rejected, nothing was published (origin/master may have moved since the check, or the remote refused the tag)." \
         "see the git output above and re-run once it's resolved. The local tag '$version' was removed."
fi
echo "Tag $version created and pushed to origin."
