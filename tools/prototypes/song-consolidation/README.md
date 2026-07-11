# PROTOTYPE — Song consolidation

This throwaway prototype asks one question:

> Can one structured Song Version representation preserve the useful content of
> both RRN JSON and OpenSong-backed songs-manager records, while giving a
> maintainer enough evidence to decide whether two records can share a version
> or must remain explicit variants?

It reads the real source files without modifying them:

- `../backend/src/main/resources/static/RRN_2022.json`
- `../../songs-manager/backend/songs_manager.db`

The candidate representation is deliberately small: song metadata, ordered
sections, lyric lines paired with chord lines, and presentation order. The
prototype is not a migration and makes no database changes.

## Run

From `Songbook/mobile`:

```powershell
python tools/prototypes/song-consolidation/prototype.py
```

For a non-interactive batch summary:

```powershell
python tools/prototypes/song-consolidation/prototype.py --summary
```

To print every exact difference for one overlapping title:

```powershell
python tools/prototypes/song-consolidation/prototype.py --title "Chlebie najcichszy"
```

Limit output to one aspect when needed:

```powershell
python tools/prototypes/song-consolidation/prototype.py --title "Chlebie najcichszy" --diff-kind chords
```

If a restricted shell cannot open the sibling repository directly, pass a
readable copy explicitly with `--manager-db <path>`.

The terminal UI lets you inspect every exact-title overlap and temporarily mark
it as shared, variant, or unresolved. Press `d` to cycle through lyric, chord,
metadata, section, and full canonical views; use `j` and `k` to page through a
long diff. Chord spaces are rendered as `·` so alignment changes are visible.
Lyric diffs ignore source line wrapping and outer whitespace but preserve wording
and punctuation; chord diffs retain line layout and make spacing visible.
Decisions live only in memory.

Delete this directory after its verdict has been captured in `NOTES.md` and the
validated parts have been absorbed into the real design.
