# Songbook

The mobile songbook context for browsing song lyrics, tags, and chords.

## Language

**Song**:
A songbook entry containing the title, optional alternate title, lyrics sections, optional chords, and tags.

**Source of Truth**:
The authoritative local song collection the app renders from. Remote refresh can update it, but screens do not render directly from remote refresh payloads.
_Avoid_: Remote result, API response
