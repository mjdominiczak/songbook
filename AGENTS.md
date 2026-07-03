# AGENTS.md

## Purpose

This repository contains the Android mobile app for Śpiewnik RRN, a simple songbook used to browse songs, lyrics, tags, and chords. Treat the current product as a read-only songbook MVP: listing songs, searching/filtering, opening song details, and changing display preferences are the main working flows.

The app is written in Kotlin with Jetpack Compose. It uses Hilt for dependency injection, Retrofit/Gson for the remote API, Navigation Compose for screen navigation, and DataStore for small user preferences.

## Current State

- The production debug app currently builds with `.\gradlew.bat assembleDebug`.
- JVM unit tests currently pass with `.\gradlew.bat testDebugUnitTest`.
- The data layer is cache-backed with Room. Songs refresh from the remote API when available and fall back to cached data after a successful sync.
- Add/edit song UI exists, but saving is not implemented. Do not assume user-created songs work.
- The list screen has a hidden add FAB and some placeholder behavior.
- Song details can display chord strings from the API, but there is no chord parsing or transposition feature.

## Build And Test Commands

Run commands from the repository root.

```powershell
.\gradlew.bat assembleDebug
```

Builds the debug APK and is the fastest basic production-code verification.

```powershell
.\gradlew.bat testDebugUnitTest
```

Runs JVM unit tests.

```powershell
.\gradlew.bat connectedDebugAndroidTest
```

Runs Android instrumentation tests when a device or emulator is available.

## Architecture Map

The app follows a light Clean Architecture / MVVM shape:

- `data`: API models, Retrofit service, Room cache, and repository implementation.
- `domain`: repository contract and use cases.
- `presentation`: Compose screens, view models, UI state, navigation, theme, reusable components.
- `di`: Hilt module wiring Retrofit, repository, and preferences.
- `resolvers`: DataStore-backed settings access.
- `json`: custom Gson adapter for polymorphic song sections.

Keep changes consistent with this structure. Use cases should contain application behavior. Repository implementations should own data-source details. View models should expose UI state and user intents without embedding network or persistence details directly.

## Important Flows

Song list:

- The list view model loads all songs via the `GetAllSongsUseCase`.
- Songs are filtered by search query and tags in memory.
- The default tag filter is currently set to the RRN 2022 songbook tag.
- Tags are derived from the loaded song list.

Song detail:

- The detail view model loads one song by route argument.
- Chord visibility and line wrapping are persisted through DataStore.
- Song sections are rendered according to their section type: simple section, verse, or chorus.

Remote API:

- The base URL is hardcoded in constants.
- Retrofit uses Gson with a custom section adapter.
- HTTP logging is currently attached in the shared API provider.

## Coding Guidelines

- Prefer small, vertical changes that keep list/detail behavior working.
- Keep Compose state ownership clear: view models own screen state; composables render state and send events back.
- Avoid adding new global state unless it is clearly application-wide and injected.
- Avoid broad refactors while fixing focused issues. This app is small enough that unnecessary abstraction will make it harder to work on.
- Prefer string resources for user-visible text.
- Keep Polish localization in sync with default strings when adding UI text.
- Do not leave debug `println` calls or one-off analysis utilities wired into production view models.
- For network behavior, surface user-meaningful error and loading states. Avoid leaking raw exception text directly into UI when adding new surfaces.

## Testing Expectations

Before claiming a change is complete, run the narrowest useful verification:

- For production-only changes, run `.\gradlew.bat assembleDebug`.
- For view model, domain, or filtering changes, run or update JVM unit tests.
- For Compose UI behavior changes, add tests where practical, or at minimum verify manually on an emulator/device if the change is visual or interaction-heavy.

If tests cannot be run or fail for unrelated reasons, state that explicitly and include the command output summary.

## Known Product Gaps

- Add/edit song is incomplete.
- Search only covers titles; alternate titles, tags, and lyrics are not fully searchable.
- Tag filtering is hardcoded around the current RRN 2022 tag assumption.
- Chord parsing/transposition is not implemented. It remains a possible future feature, but should be rebuilt as tested domain behavior rather than debug list-loading code.
- Release hardening is minimal: minification is off, API configuration is hardcoded, and logging is not build-type-specific.
- README is brief and still describes the project as early-stage.

## Good Next Directions

1. Add a local song cache and offline-first loading.
2. Decide whether add/edit belongs in the mobile app. Either implement it end to end or remove the visible placeholder.
3. Improve song discovery with richer search and explicit songbook/category filters.
4. Consider chord parsing/transposition as a tested user-facing feature.
5. Split debug/release API and logging behavior before a production release.
6. Expand test coverage around filtering, preferences, and chord display behavior as those areas are changed.

## Agent Notes

- This repository may have user changes. Check `git status --short` before editing and do not revert unrelated work.
- Use `rg` for searching.
- Use Gradle wrapper commands rather than assuming a system Gradle install.
- Use Conventional Commit messages for commits, for example `fix: handle empty song list`, `test: update coroutine rule`, or `docs: refresh agent notes`.
- Network-dependent changes may require care because the app currently depends on the remote API for real content.
- If changing dependencies or Android Gradle Plugin configuration, verify both app build and test compilation.

## Agent skills

### Issue tracker

Issues are tracked in GitHub Issues for `mjdominiczak/songbook`; external PRs are not a triage request surface. See `docs/agents/issue-tracker.md`.

### Triage labels

The canonical triage labels are used unchanged: `needs-triage`, `needs-info`, `ready-for-agent`, `ready-for-human`, and `wontfix`. See `docs/agents/triage-labels.md`.

### Domain docs

This is a single-context repo: use root `CONTEXT.md` and `docs/adr/` when they exist. See `docs/agents/domain.md`.
