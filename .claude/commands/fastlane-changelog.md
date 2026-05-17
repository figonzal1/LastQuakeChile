# Fastlane Changelog Generator

Generate the Fastlane changelog files before a Google Play release.

> **Note:** This project uses release-please, so the exact next versionCode is unknown until the release PR merges. Changelogs are always written to `default.txt` — Fastlane uses it as a fallback when no versionCode-specific file is found.

## Steps

1. Check if `fastlane/metadata/android/` exists. If not, create the full directory tree:
   - `fastlane/metadata/android/es-419/changelogs/`
   - `fastlane/metadata/android/en-US/changelogs/`

2. Check if `default.txt` already exists in both locales. If it does, show its content and ask the user if they want to overwrite it.

3. Extract changes from git commits:
   - Find the latest git tag with `git describe --tags --abbrev=0`. If no tag exists, use the first commit.
   - Run `git log <tag>..HEAD --oneline --no-merges` to get all commits since that tag.
   - Filter and interpret the commits following these rules:
     - **Include:** `feat`, `fix`, `perf` — these are user-visible changes.
     - **Exclude:** `chore`, `refactor`, `test`, `ci`, `build`, `docs` — internal/technical, not relevant to end users.
     - **Simplify language:** convert technical commit messages into plain, user-friendly phrases. Never expose library names, internal module names, class names, or implementation details (e.g. "migrate Coil 3.4.0" → "mejoras en la carga de imágenes").
     - **Group similar changes** into a single bullet instead of listing each commit separately.
     - If after filtering no user-visible commits remain, say so and ask the user to provide a brief description manually.
   - Show the raw filtered commit list to the user before generating the notes, so they can flag anything that should be excluded or reworded.

4. Generate polished, concise release notes (max 500 characters each) in:
   - **Spanish (es-419):** simple and friendly tone, suitable for Chilean/Latin American users.
   - **English (en-US):** matching tone and content.
   - Show both drafts to the user and ask for confirmation or edits before writing.

5. Write the confirmed content to:
   - `fastlane/metadata/android/es-419/changelogs/default.txt`
   - `fastlane/metadata/android/en-US/changelogs/default.txt`

6. Report a summary:
   - Files created or updated
   - Character count for each changelog (reminder: Google Play limit is 500 chars)
   - Next step hint: run `fastlane beta_googleplay` or `fastlane prod_googleplay`
