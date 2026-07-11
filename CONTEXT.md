# Songbook

The songbook context for maintaining and browsing shared song content across curated songbooks and community repertoires.

## Language

**Song**:
The shared identity of a composition, independent of a particular community's lyrics, chords, or arrangement.
_Avoid_: Songbook entry, repertoire entry

**Song Version**:
A concrete rendition of a Song containing lyrics, chords, attribution, and section order. Identical content may be shared across communities, while intentional differences remain separate versions.
_Avoid_: Song copy, local song

**Songbook**:
A named, curated collection of Song Versions intended for readers.
_Avoid_: Tag, repertoire

**Songbook Entry**:
The membership of a selected Song Version in a Songbook, including its index within that Songbook.
_Avoid_: Song

**Repertoire Entry**:
A community's use of a selected Song Version together with private operational information such as ratings, notes, classifications, setlists, and performance history.
_Avoid_: Song, songbook entry

**Community**:
The ownership and access boundary for a repertoire, setlists, classifications, notes, and performance history.
_Avoid_: User, songbook

**Community Membership**:
The association that grants a User access to one Community with a role specific to that Community. A User may hold memberships in multiple Communities.
_Avoid_: Global role, user scope

**Source of Truth**:
The authoritative local song collection the app renders from. Remote refresh can update it, but screens do not render directly from remote refresh payloads.
_Avoid_: Remote result, API response
