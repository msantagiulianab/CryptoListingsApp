#!/usr/bin/env bash
#
# release.sh — interactive release workflow for CryptoListingsApp
#
# Usage:  ./scripts/release.sh [REMOTE]
#
#   REMOTE   optional git remote to push the tag to (default: origin)
#
# This script is a self-contained, pure-Bash implementation with no Node/npm
# dependencies. It verifies the working tree is clean, asks for a semantic
# version tag (e.g. v0.1.0-dev.1), double-confirms it, and pushes the tag
# upstream. Pushing a tag matching 'v*' triggers the
# .github/workflows/build-android.yml workflow, which compiles an unsigned
# debug APK and attaches it to a GitHub Release.
#
# Requirements:
#   - Bash 4+
#   - git (available on PATH)
#   - A clean local working tree

set -euo pipefail

REMOTE="${1:-origin}"
TAG_REGEX='^v[0-9]+\.[0-9]+\.[0-9]+(-[0-9A-Za-z.-]+)?(\+[0-9A-Za-z.-]+)?$'

# ---- Colours (auto-disabled when piped or when NO_COLOR is set) ------------
if [ -t 1 ] && [ -z "${NO_COLOR:-}" ]; then
    BOLD=$'\e[1m'; RED=$'\e[31m'; GREEN=$'\e[32m'; YELLOW=$'\e[33m'; CYAN=$'\e[36m'; RESET=$'\e[0m'
else
    BOLD=''; RED=''; GREEN=''; YELLOW=''; CYAN=''; RESET=''
fi

die()  { printf '%s\n' "${RED}❌ ERROR:${RESET} $*" >&2; exit 1; }
info() { printf '%s\n' "${CYAN}ℹ️  $*${RESET}"; }
ok()   { printf '%s\n' "${GREEN}✅ $*${RESET}"; }
warn() { printf '%s\n' "${YELLOW}⚠️   $*${RESET}"; }

ask() {
    # ask "prompt" -> echoes the trimmed answer (may be empty)
    # The prompt goes to STDERR so callers using $(ask ...) only capture the
    # answer on stdout.
    local reply=""
    printf '%s ' "$1" >&2
    read -r reply || true
    reply="${reply#"${reply%%[![:space:]]*}"}"   # strip leading whitespace
    reply="${reply%"${reply##*[![:space:]]}"}"   # strip trailing whitespace
    printf '%s' "$reply"
}

# ---------------------------------------------------------------------------
# 0. Pre-flight checks
# ---------------------------------------------------------------------------
command -v git >/dev/null 2>&1 || die "git is required but was not found on PATH."

git rev-parse --is-inside-work-tree >/dev/null 2>&1 \
    || die "not inside a Git working tree."

# ---------------------------------------------------------------------------
# 1. Reject if the working tree is dirty (checked before anything else)
# ---------------------------------------------------------------------------
if [ -n "$(git status --porcelain)" ]; then
    warn "Working tree is dirty — commit or stash your changes before releasing:"
    git status --short
    exit 1
fi

git remote get-url "$REMOTE" >/dev/null 2>&1 \
    || die "remote '$REMOTE' does not exist. Available remotes: $(git remote)"
REMOTE_URL="$(git remote get-url "$REMOTE")"

# ---------------------------------------------------------------------------
# 2. Release checklist
# ---------------------------------------------------------------------------
printf '\n'
printf '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n'
printf '  %s📋 Release checklist%s\n' "$BOLD" "$RESET"
printf '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n'
printf '  1. Local build succeeds                (./gradlew assembleDebug)\n'
printf '  2. Smoke tests pass on a device/emulator\n'
printf '  3. Changes since the last tag are intentional\n'
printf '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n'
printf '\n'
REPLY="$(ask "Have you verified the checklist above? (y/N)")"
case "${REPLY,,}" in
    y|yes) ;;
    *) printf '%s\n' "⚠️   Release aborted — complete the checklist first."; exit 0 ;;
esac

# ---------------------------------------------------------------------------
# 3. Ask for the semantic release tag
# ---------------------------------------------------------------------------
TAG_NAME=""
while [ -z "$TAG_NAME" ]; do
    TAG_NAME="$(ask "Enter a semantic version tag (e.g. v0.1.0-dev.1):")"
    if [ -z "$TAG_NAME" ]; then
        warn "Tag name cannot be empty."
    elif ! printf '%s' "$TAG_NAME" | grep -Eq "$TAG_REGEX"; then
        warn "Invalid semantic version '$TAG_NAME' — expected e.g. v0.1.0 or v0.1.0-dev.1"
        TAG_NAME=""
    elif git rev-parse -q --verify "refs/tags/$TAG_NAME" >/dev/null 2>&1; then
        warn "Tag '$TAG_NAME' already exists locally."
        TAG_NAME=""
    elif git ls-remote --tags "$REMOTE" "refs/tags/$TAG_NAME" 2>/dev/null | grep -q .; then
        warn "Tag '$TAG_NAME' already exists on '$REMOTE'."
        TAG_NAME=""
    fi
done

# ---------------------------------------------------------------------------
# 4. Double-confirm
# ---------------------------------------------------------------------------
printf '\n'
printf '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n'
printf '  %s🚀 Ready to create and push tag:  %s%s\n' "$BOLD" "$TAG_NAME" "$RESET"
printf '  Remote:  %s\n' "$REMOTE_URL"
printf '  Commit:  %s  (%s)\n' "$(git rev-parse --short HEAD)" "$(git rev-parse --abbrev-ref HEAD)"
printf '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n'
printf '\n'
CONFIRM_TAG="$(ask "Type the tag name again to confirm:")"
if [ "$CONFIRM_TAG" != "$TAG_NAME" ]; then
    printf '%s\n' "⚠️   Tag mismatch — release aborted."
    exit 0
fi

# ---------------------------------------------------------------------------
# 5. Create the annotated tag and push it upstream
# ---------------------------------------------------------------------------
cleanup_on_failure() {
    # Remove the local tag if the push never made it upstream.
    if git rev-parse -q --verify "refs/tags/$TAG_NAME" >/dev/null 2>&1 \
        && ! git ls-remote --tags "$REMOTE" "refs/tags/$TAG_NAME" 2>/dev/null | grep -q .; then
        warn "Push failed — removing local tag '$TAG_NAME'."
        git tag -d "$TAG_NAME" >/dev/null 2>&1 || true
    fi
}
trap cleanup_on_failure EXIT

printf '\n'
printf '  🏷️  Creating annotated tag: %s ...\n' "$TAG_NAME"
git tag -a "$TAG_NAME" -m "Release $TAG_NAME"

printf '  📤 Pushing tag to %s ...\n' "$REMOTE"
git push "$REMOTE" "$TAG_NAME"

printf '\n'
printf '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n'
ok "Tag $TAG_NAME pushed successfully!"
printf '\n'
printf '  GitHub Actions will now:\n'
printf '     → Compile an unsigned debug APK via ./gradlew assembleDebug\n'
printf '     → Attach it to a GitHub Release for this tag\n'
printf '\n'
case "$REMOTE_URL" in
    *github.com*)
        REPO_PATH="${REMOTE_URL#*github.com/}"
        REPO_PATH="${REPO_PATH%.git}"
        printf '  Watch: https://github.com/%s/actions\n' "$REPO_PATH"
        ;;
    *)
        printf '  Watch: your CI dashboard for this repository\n'
        ;;
esac
printf '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n'
