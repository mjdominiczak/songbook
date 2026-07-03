# Offline Song Cache Plan

## Summary

Add an offline-first song cache so the app can show both the song list and song detail screens after one successful sync. The first implementation uses Room, refreshes from the remote API when available, and keeps the current repository/use case API shape stable for view models.

Default choices: list and detail cache, cache-backed repository behavior, and hybrid local storage with searchable song metadata as columns plus full song content as JSON.

## Key Changes

- Add Room dependencies using the existing Gradle and `kapt` setup.
- Store songs in a local `songs` table keyed by remote song id.
- Keep metadata such as title, alternate title, info, transposition, and update time as columns.
- Store tags and polymorphic song content as JSON using the same Gson section adapter as the remote API.
- Update the repository to persist successful API responses and fall back to cached data when the network fails.
- Keep list and detail view models on the existing use cases so UI behavior changes stay limited to data availability.

## Cache Behavior

- `getAllSongs()` attempts a remote refresh and stores successful results.
- If the all-songs refresh fails and cached songs exist, return cached songs.
- If the all-songs refresh fails and the cache is empty, surface the network error through the existing `Resource.Error` path.
- `getSongById(id)` attempts a remote refresh for that song and stores successful results.
- If the detail refresh fails and the song is cached, return the cached song.
- If the detail refresh fails and the song is not cached, surface the network error through the existing `Resource.Error` path.

## Test Plan

- Repository tests cover successful refresh, cache persistence, cached fallback, and empty-cache network failure for both list and detail loading.
- Mapper tests cover tags, nullable content, section variants, chords, and metadata round-tripping through the cache entity.
- Existing view model tests remain unchanged and verify the public use case contract still works.
- Final verification commands:
  - `.\gradlew.bat testDebugUnitTest`
  - `.\gradlew.bat assembleDebug`

## Future Work

- Add a manual refresh action and user-facing sync state if stale data becomes confusing.
- Add cache age policy or background sync only after real usage shows the need.
- Expand search against cached alternate titles, tags, and lyrics after the offline cache is stable.
- Do not include offline add/edit behavior in this cache slice.
- Revisit Room schema export when migration work starts; it is intentionally off for the first cache version.
