# CLAUDE.md

Guidance for Claude Code when working in this repository.

## Project overview

Android app (Kotlin) showing recent earthquakes in Chile. Single Gradle module `:app`.

- **DI:** Koin (not Hilt)
- **Networking:** Retrofit + Moshi + Sandwich (`ApiResponse`)
- **Persistence:** Room (KSP codegen)
- **Images:** Coil · **Maps:** Google Maps + android-maps-utils (marker clustering)
- **Backend services:** Firebase (Crashlytics, Performance, FCM)
- **UI:** XML views, with an incremental XML → Compose migration in progress (see PR #80)

### Architecture

Feature-based packages under `app/src/main/java/cl/figonzal/lastquakechile/`:

- `core/` — shared infra: DI modules, networking, DB, utils, `DomainResult`/`DomainError`
- `quake_feature/` — earthquake list, detail, and map (data/domain/ui)
- `reports_feature/` — reports feature (data/domain/ui)

Data flow: remote/local data sources return domain models via mappers; repositories
expose `DomainResult<T>` (success/error), surfaced to ViewModels as a single `uiState`.

## Commands

Build uses product flavors `dev` / `beta` / `prod` (dimension `version`) and build types `debug` / `release`.

The toolchain is pinned in `mise.toml`: **Java temurin-21** (Gradle JVM — `compileOptions` and
`jvmTarget` stay at 17, that is the bytecode level) and **Ruby 3.4** (fastlane/bundler).

```bash
./gradlew assembleDevDebug          # build dev/debug APK
./gradlew test                      # all unit tests
./gradlew :app:connectedDevDebugAndroidTest   # instrumentation tests (device/emulator)
./gradlew bundleProdRelease         # production AAB

# fastlane wrappers (bundle exec):
bundle exec fastlane unit_test      # unit tests
bundle exec fastlane ui_test        # instrumentation tests (DevDebug)
bundle exec fastlane prod_googleplay # build prod AAB + upload to Google Play
```

## Gotchas

- Requires `secrets.properties` (Maps API key via Secrets Gradle plugin) and signing material
  under `keys/` — both untracked; builds fail without them.
- Destructive Room migration and HTTP logging are gated to debug builds only.
- `versionCode` is derived from the version managed by release-please — do not hardcode it.
- Signing comes **only** from `keys/keystore.properties`, read by the `lastquakechilesign`
  `signingConfig`. Fastlane deliberately does not inject `android.injected.signing.*` — a second
  copy of the keystore path drifts per machine, and `-P` properties leak the passwords into the
  build log. The only property the build lanes pass is `uploadMapping`.
- The Crashlytics mapping is uploaded **only** when `-PuploadMapping` is set (the `beta` and `prod`
  fastlane lanes pass it). Local release builds skip it so they don't overwrite the mapping of the
  same `versionCode` in Firebase.

## Release process (release-please + fastlane + Google Play)

This project releases through [release-please](https://github.com/googleapis/release-please)
(`release-type: simple`) and ships to Google Play via fastlane.

### Branch rules

- `main` has an **active ruleset** (`pull_request` required, `non_fast_forward`).
  Direct pushes to `main` are **blocked for everyone** — all changes land via PR.
- `required_approving_review_count: 0`, so the owner can **self-merge** PRs without approval.
- Only `main` is protected; feature and release-please branches are unrestricted.

### How release-please behaves

- The workflow `.github/workflows/release-please.yml` runs on **every push to `main`**
  (and supports `workflow_dispatch` for manual runs).
- It does **not** cut a release on each run. It maintains a **single rolling release PR**
  on the branch `release-please--branches--main`.
- The git tag, GitHub Release, and `versionCode` bump happen **only when that release PR is merged**.
- `chore`, `refactor`, `ci`, `docs`, etc. do **not** bump the version — only `feat`/`fix` do.
  Adding such commits just refreshes the open release PR; it never spawns a second release.

### Fastlane changelogs — when and where

Use the `/fastlane-changelog` command (see `.claude/commands/fastlane-changelog.md`).

- **Timing:** generate the changelogs **before merging the release PR**, while the latest git
  tag is still the **previous** version. The command diffs `git describe --tags --abbrev=0..HEAD`;
  if run after the release PR merges, the new tag already exists and the diff is empty.
- Changelogs are always written to `default.txt` (locales `es-419` and `en-US`), which fastlane
  uses as a fallback when no versionCode-specific file exists.
- **Convention (owner's preference):** commit the `default.txt` changelogs as the **last commit
  directly onto the `release-please--branches--main` branch**, so they ship inside that same
  release PR. Only do this when no further pushes to `main` are expected before the merge — a later
  release-please run can regenerate/force-push that branch and drop the commit.

### End-to-end release flow

1. Wait for release-please to open/update the release PR (`release-please--branches--main`).
2. Run `/fastlane-changelog` (previous tag still latest) → writes `default.txt` for `es-419` + `en-US`.
3. Commit those `default.txt` files as the final commit onto the release PR branch.
4. **Merge the release PR** (`gh pr merge <n> --merge` or the GitHub UI) → creates the tag,
   GitHub Release, and `versionCode` bump.
5. Build and upload: `fastlane prod_googleplay` (or `beta_googleplay`) — reads `default.txt`.

## Conventions

- Write GitHub issues in **English**, regardless of conversation language.
