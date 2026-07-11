# Prototype verdict

**Question:** Can one structured Song Version representation preserve the useful
content of both RRN JSON and OpenSong-backed songs-manager records, while
exposing enough evidence for shared-version versus explicit-version review?

**Status:** Answered after batch analysis and hands-on diff review.

## Observations

- The real datasets contain 47 exact-title overlaps.
- Only one overlap currently meets the prototype's strict shared-version threshold
  for both lyrics and chords; 42 look like explicit-version candidates and four
  still need manual identity review.
- Chord coverage is asymmetric: every RRN record contains chord data, while only
  20 of 159 songs-manager records contain OpenSong chord lines. Missing Schola
  chords are therefore often missing information, not proof of a conflicting
  version.
- Among overlaps, presentation order differs or is unavailable in RRN for 33
  songs, and section counts differ for seven.
- RRN's general `info` string and songs-manager's structured author/key/meter
  fields need an explicit metadata mapping.
- Exact-title and high lyric similarity are useful review signals, but are not
  sufficient to merge Song Versions automatically.

## Verdict

- Consolidation should be an enrichment-and-review pipeline, not an automatic
  duplicate merger.
- When records represent the same Song and one source only lacks information,
  merge the complementary information into one shared Song Version. Missing
  chords are missing data, not a reason to create a separate version.
- Differences caused only by representation, such as line wrapping or chord-line
  grouping, should be normalized rather than treated as content conflicts.
- Genuine unresolved differences in lyrics, chords, attribution, section order,
  or other content require maintainer review. The review either produces one
  reconciled shared Song Version or preserves explicit versions.
- Similarity scores may prioritize the review queue, but never decide identity or
  overwrite conflicting content by themselves.
- The prototype does not settle whether the persisted canonical format should be
  structured sections, raw OpenSong, or both. That is now a design decision, not
  something further comparison scoring will answer.

## What to keep

- The distinction between Song identity, Song Version, and the entries that
  adopt a version.
- Maintainer review for probable matches.
- Separate lyric, chord, section, presentation, and metadata reconciliation
  signals instead of one aggregate duplicate score.
- The field-level distinction between `missing`, `equivalent after
  normalization`, and `conflicting`.

## What to delete

- The terminal shell and heuristic thresholds after the decision is captured in
  the implementation design and the prototype is preserved on its throwaway
  branch.
