# Fastlane Changelog Generator

Generate the Fastlane changelog files for the current app version before a Google Play release.

## Steps

1. Read `app/build.gradle.kts` and extract `appVersionName` (format `"X.Y.Z"`).

2. Calculate `versionCode` using the same formula as the build file:
   `vMajor * 1_000_000 + vMinor * 1_000 + vPatch`

3. Check if `fastlane/metadata/android/` exists. If not, create the full directory tree:
   - `fastlane/metadata/android/es-419/changelogs/`
   - `fastlane/metadata/android/en-US/changelogs/`

4. Check if a changelog for the current `versionCode` already exists in both locales. If it does, show its content and ask the user if they want to overwrite it.

5. Extract changes from git commits:
   - Find the latest git tag with `git describe --tags --abbrev=0`. If no tag exists, use the first commit.
   - Run `git log <tag>..HEAD --oneline --no-merges` to get all commits since that tag.
   - Filter and interpret the commits following these rules:
     - **Include:** `feat`, `fix`, `perf` — these are user-visible changes.
     - **Exclude:** `chore`, `refactor`, `test`, `ci`, `build`, `docs` — internal/technical, not relevant to end users.
     - **Simplify language:** convert technical commit messages into plain, user-friendly phrases. Never expose library names, internal module names, class names, or implementation details (e.g. "migrate Coil 3.4.0" → "mejoras en la carga de imágenes").
     - **Group similar changes** into a single bullet instead of listing each commit separately.
     - If after filtering no user-visible commits remain, say so and ask the user to provide a brief description manually.
   - Show the raw filtered commit list to the user before generating the notes, so they can flag anything that should be excluded or reworded.

6. Generate polished, concise release notes (max 500 characters each) in:
   - **Spanish (es-419):** simple and friendly tone, suitable for Chilean/Latin American users.
   - **English (en-US):** matching tone and content.
   - Show both drafts to the user and ask for confirmation or edits before writing.

6. Write the confirmed content to:
   - `fastlane/metadata/android/es-419/changelogs/<versionCode>.txt`
   - `fastlane/metadata/android/en-US/changelogs/<versionCode>.txt`

7. Also ensure `default.txt` exists in both locale folders (copy from the new file if it doesn't exist yet).

8. Report a summary:
   - Version name and code used
   - Files created or updated
   - Character count for each changelog (reminder: Google Play limit is 500 chars)
   - Next step hint: run `fastlane beta_googleplay` or `fastlane prod_googleplay`
