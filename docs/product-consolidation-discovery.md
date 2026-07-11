# Songbook / Schola Product Consolidation Discovery

**Status:** Discovery draft — awaiting product-owner answers
**Created:** 2026-07-11
**Repositories reviewed:**

- `Songbook/mobile` — Android reader for Śpiewnik RRN
- `Songbook/backend` — read-only API used by the Android app
- `songs-manager` — Schola repertoire-management web application and API

## Purpose

This report captures the product and domain context inferred from the three repositories. Its goal is to support a decision about whether and how the projects should be consolidated.

The findings below distinguish:

- facts evidenced by the current code and repository documentation;
- working interpretations that still require product confirmation;
- unresolved questions for the product owner.

This is not yet an implementation plan or architecture decision.

## Executive summary

The projects have meaningful content overlap but currently serve different primary jobs:

- The Android app is a simple, anonymous, offline-capable consumption surface for a published songbook.
- songs-manager is an authenticated operator workspace for maintaining one community's repertoire, importing and exporting songs, planning Mass setlists, and tracking performance history.
- The old Songbook backend is effectively a JSON publication service. It loads a bundled RRN 2022 dataset and exposes read-only song endpoints.

The most promising consolidation direction is therefore to share the backend content and publication pipeline while retaining separate user experiences for readers and repertoire managers.

A tentative direction is to evolve songs-manager's backend so that it can publish versioned, read-only songbooks in addition to managing private community repertoire data. The Android app could consume that public API or a downloadable publication snapshot. The small Kotlin/Spring backend could then be retired.

This recommendation depends on unresolved questions about editorial ownership, content licensing, community isolation, content variants, and the intended future of the Android app.

## Current product map

| Area | Śpiewnik RRN mobile/backend | songs-manager / Schola |
|---|---|---|
| Primary user | Person looking up lyrics and chords | Person maintaining and planning a choir repertoire |
| Primary job | Find and read a song | Edit songs, prepare Mass setlists, and manage repertoire |
| Access model | Anonymous reader | Authenticated web user |
| Offline behavior | Room-backed local source of truth after successful synchronization | Online web application |
| Content scope | One RRN 2022 collection with 295 songs | One operational repertoire with 159 songs in the inspected local database |
| Editing | Add/edit UI exists, but saving is not implemented | Song CRUD is implemented |
| Song representation | Explicit simple/verse/chorus sections; separate aligned chord strings | OpenSong-style lyrics with section markers and dot-prefixed chord lines |
| Collection concepts | `RRN 2022` is currently represented as a tag | No first-class Songbook model found |
| Community-specific metadata | Minimal | Ratings, thematic tags, Mass parts, notes, setlists, and performance history |
| Import/export | None in the mobile product | OpenSong, Serafin, folder import, and historical CSV import |
| Presentation | Song-detail reading only | Fullscreen song/setlist presentation is the active planned milestone |
| Backend maturity | Small read-only Spring Boot service using bundled JSON and H2 | Deployed FastAPI application with SQLAlchemy, Alembic, PostgreSQL support, auth, and broader API surface |
| Community/tenant ownership | Not modeled | Authentication exists, but songs and operational data are global rather than scoped to a workspace or community |

## Evidence from the repositories

### Android application

The mobile application currently supports the reader experience:

- listing songs;
- title search;
- in-memory tag filtering;
- song-detail rendering;
- chord visibility and line-wrapping preferences;
- cache-backed reading through Room;
- remote refresh with cached fallback after successful synchronization.

The default filter is the `RRN 2022` tag. The view model excludes that tag from the ordinary tag-filter list, which indicates that it is being used as a provisional songbook identifier rather than as an ordinary descriptive tag.

The app's repository observes Room and writes remote refresh results into Room. This gives a contained migration seam: changing the publishing API does not inherently require changing the reader's offline-first behavior.

Add/edit UI is present, but repository saving is a `TODO`. It should not currently be treated as part of the working product.

Relevant files:

- `app/src/main/java/com/mjdominiczak/songbook/data/SongRepositoryImpl.kt`
- `app/src/main/java/com/mjdominiczak/songbook/data/local/SongEntity.kt`
- `app/src/main/java/com/mjdominiczak/songbook/presentation/list/SongListViewModel.kt`
- `app/src/main/java/com/mjdominiczak/songbook/presentation/detail/SongDetailScreen.kt`

### Songbook backend

The Spring Boot backend:

- loads `static/RRN_2022.json` during startup;
- persists the loaded data to its repository;
- exposes `GET /songs` and `GET /songs/{id}`;
- contains no working authoring, publication, user, or multi-songbook workflow.

Its production value is primarily the API contract and hosting of a fixed dataset. The repository has seen content updates, but little backend product development since 2024.

Relevant files:

- `../backend/src/main/resources/static/RRN_2022.json`
- `../backend/src/main/kotlin/com/mjdominiczak/songbook_backend/data/LoadDatabase.kt`
- `../backend/src/main/kotlin/com/mjdominiczak/songbook_backend/SongController.kt`

### songs-manager

songs-manager is a substantially broader operational product. Implemented or documented capabilities include:

- song and tag CRUD;
- OpenSong song import and export;
- Serafin export;
- folder import;
- setlist CRUD;
- historical CSV import with matching, preview, and conflict handling;
- performance counts and last-performance tracking;
- Mass-part classification;
- liturgical-data retrieval;
- authentication and deployment;
- a planned smart-setlist workflow;
- a currently planned fullscreen presentation mode for songs and whole setlists.

The inspected local database contains:

- 159 songs;
- 223 setlists;
- 862 performance records;
- 10 predefined tags.

Authentication protects the API, but the authenticated user is not used to scope queries. All authenticated users currently operate on the same global song, tag, setlist, and performance data.

Relevant files:

- `../../songs-manager/backend/app/models/song.py`
- `../../songs-manager/backend/app/models/setlist.py`
- `../../songs-manager/backend/app/api/songs.py`
- `../../songs-manager/backend/app/api/setlists.py`
- `../../songs-manager/.dev/ROADMAP.md`
- `../../songs-manager/.dev/m012-presentation-mode/MILESTONE.md`
- `../../songs-manager/docs/features/2025-10-01-prd.md`

## Content overlap analysis

The scan found substantial overlap between the two song collections:

- RRN 2022 contains 295 songs.
- The inspected songs-manager database contains 159 songs.
- 47 songs-manager titles exactly match an RRN title.
- A heuristic comparison removed OpenSong section markers and chord lines, normalized text, and compared unique lyric tokens.
- 42 of the 47 title matches had at least 0.80 token-set similarity.
- The remaining five had similarity between 0.63 and 0.78, suggesting textual variants, incomplete versions, or representation differences.

This is strong evidence that shared content reuse could be valuable. It is not sufficient evidence for automatically merging records.

For example, `Chlebie najcichszy` contains effectively the same lyric and chord content in both systems, but:

- RRN stores a list of typed sections with separate text and chord strings;
- songs-manager stores OpenSong text with `[C]` and `[V1]` headings, alternating chord lines, and an explicit presentation order of `C V1 C V2 C`;
- RRN stores author/meter information in a general `info` string;
- songs-manager stores author, key, time signature, and presentation order in dedicated fields.

The overlap suggests that conversion and reconciliation are feasible, but the domain needs to account for community-specific versions.

## Domain distinctions needed for consolidation

The word **Song** currently refers to several different concepts. A consolidated model may need to distinguish the following terms.

### Song identity

The generally recognized composition, independent of one community's exact lyrics, chords, key, or arrangement.

Example: the abstract identity of `Chlebie najcichszy`.

This concept may be useful for discovering equivalent content across communities, but it should not be introduced until the required sharing behavior is confirmed.

### Song version

A concrete editable representation of lyrics, chords, attribution, section structure, and presentation order. Two communities may use different song versions while recognizing them as the same underlying composition.

Possible alternative names include **Arrangement**, **Edition**, or **Content Version**. The correct term depends on whether musical arrangement, editorial publication, or simple local variation is the dominant distinction.

### Repertoire entry

A community's working use of a song version. It can carry private operational information such as:

- rating;
- local notes;
- suitable Mass parts;
- local thematic tags;
- performance history;
- setlist membership.

These fields should not automatically become global properties of a shared song.

### Songbook

A curated collection such as `RRN 2022`. Songbook membership should be explicit rather than encoded as an ordinary tag if multiple collections are supported.

### Publication

A versioned release of a Songbook made available to readers. A Publication may need stable identifiers, an editorial state, a release date, and reproducibility after later corrections.

### Workspace or community

The organization whose users, repertoire entries, setlists, notes, and performance history belong together. This concept does not currently exist in either persistence model.

## Candidate consolidation strategies

### Option A: Shared backend, separate product surfaces

Evolve songs-manager's backend into a platform supporting both:

- authenticated community repertoire management;
- public or otherwise distributable, read-only Songbook publications.

The Android app remains a focused reader and consumes a publication API or snapshot. The existing Songbook backend is retired after migration.

**Likely benefits**

- One content ingestion and correction pipeline.
- Continued purpose-built experiences for readers and managers.
- Reuse of the more mature backend, database, imports, and migrations.
- A path toward multiple songbooks without making the Android app an editing tool.

**Principal costs and risks**

- songs-manager needs explicit publication concepts.
- Multi-community management would require workspace ownership and authorization, not just authentication.
- Public reader data must be separated from private operational data.
- Existing song representations require a deliberate conversion contract.

**Current assessment:** Tentatively preferred, subject to the unanswered questions below.

### Option B: Shared publication pipeline only

Keep the two operational domains separate. Add an export process to songs-manager that produces a versioned artifact in the Android application's required format. Continue serving that artifact from the existing backend or static hosting.

**Likely benefits**

- Faster and lower-risk than a shared platform.
- Avoids immediate tenant/workspace design.
- Still reduces duplicate content-maintenance tooling.

**Limitations**

- Two backends remain.
- Cross-community reuse remains manual or export-driven.
- The old backend continues to require maintenance unless replaced by static hosting.

### Option C: Separate shared-content service

Create a dedicated service for shared song identities, song versions, and Songbooks. Schola retains a separate operational backend and references shared content.

**Likely benefits**

- Strong separation between publishable content and private community operations.
- Independent lifecycle for shared catalog data.

**Limitations**

- More services, deployments, synchronization, and failure modes.
- Probably excessive for the current product scale.

### Option D: One application and one user experience

Replace the separate reader and manager experiences with one responsive web application or PWA.

**Likely benefits**

- Fewer client applications.
- One deployment surface.

**Limitations**

- Reader and operator jobs are substantially different.
- Risks losing Android's focused and offline-capable experience.
- Creates a larger product redesign rather than a consolidation of shared capabilities.

**Current assessment:** Not recommended as the starting point.

## Tentative incremental route

The following sequence can deliver consolidation value before committing to full multi-community architecture:

1. Resolve the editorial, licensing, ownership, and product questions below.
2. Define whether a Songbook is a fixed editorial release or a live view of a repertoire.
3. Build a one-off RRN conversion and reconciliation proof of concept.
4. Produce a report of exact matches, probable matches, variants, and conflicts; do not automatically merge by title.
5. Add first-class Songbook and Publication concepts to songs-manager or a publication module adjacent to it.
6. Expose a versioned, read-only publication API or downloadable snapshot.
7. Adapt Android's remote data source while preserving its Room-backed local source of truth.
8. Run the old and new publication sources in parallel and compare their complete output.
9. Retire the Spring backend after Android migration and data verification.
10. Introduce workspace isolation only if multiple independently managed communities are an actual near-term product need.
11. Add cross-community song identity and reuse workflows only after real content conflicts have informed the model.

## Decision review — 2026-07-11

The product-owner answers support **Option A: shared backend with separate product surfaces**.

The intended initial product shape is:

- one extensible FastAPI/PostgreSQL backend;
- an end-user Android product for multiple Songbooks, with offline reading and device-local favorites;
- an operational product for maintainers and collaborators;
- maintainer-created community setlists, potentially authorable from either the web application or Android when the user has the required role;
- community-private ratings, notes, tags, setlists, and performance history;
- users associated with one or more Communities through role-bearing Community Memberships;
- an all-accessible-communities view for users who belong to several Communities, while every private record retains one owning Community;
- retirement of the Kotlin/Spring backend after migration;
- no backward-compatibility requirement for the existing mobile API.

The agreed sharing rule is:

1. Entries in different Songbooks or community repertoires may reference the same Song Version when their content is identical.
2. Editing shared content creates a new Song Version.
3. The maintainer explicitly chooses which Songbook Entries and Repertoire Entries adopt the new version.
4. Entries that do not adopt it remain on their existing version, allowing deliberate community forks.
5. Likely duplicate Songs and Versions are resolved through maintainer review, not title-only automatic merging.

The canonical content representation is confirmed as a structured Song Version containing sections, lyric lines, associated chords, and presentation order. OpenSong is an import/export format with semantic rather than byte-for-byte round-trip compatibility.

### Prototype verdict — 2026-07-12

The conversion prototype established the reconciliation policy:

_Primary source:_ the runnable throwaway prototype is preserved outside the product branch on `prototype/song-consolidation` under `tools/prototypes/song-consolidation/`.

1. Candidate matches are reviewed as the same Song identity before their content is combined.
2. Missing information is enrichment, not a conflict. For example, an otherwise matching Schola record without chords may adopt the RRN chords in one shared Song Version.
3. Representation-only differences such as line wrapping or chord-line grouping are normalized.
4. Genuine content differences are never overwritten automatically. A maintainer reconciles them into one version or preserves explicit Song Versions.
5. Similarity scores prioritize review but do not merge records or select a winner.

The prototype no longer needs additional matching heuristics. The structured canonical format and the field-level reconciliation policy are confirmed; OpenSong compatibility remains an import/export verification requirement.

### Community access model — 2026-07-12

Community access is many-to-many rather than a single community field on the User:

1. A User receives access through one or more Community Memberships.
2. Each membership carries the User's role in that Community, allowing the same User to be a viewer in one Community and a maintainer in another.
3. A User may view the union of content from all Communities they can access, with Community filters and provenance retained in the UI.
4. Repertoire Entries, community tags and notes, Setlists, and performance history each belong to exactly one Community.
5. Shared Songs and Song Versions may be referenced by multiple Communities without duplicating content.
6. Every write occurs within an explicit Community context and is authorized against that membership; an aggregate “all communities” view is not itself an ownership scope.
7. Public Songbook content may additionally be readable without Community Membership when its publication visibility permits it.

The current songs-manager authentication does not implement this boundary: authenticated users query one global dataset. Community Membership and query/write scoping are therefore required backend migration work, not merely UI filtering.

## Questions for asynchronous answers

Please write answers directly below each question. `TBD` can remain where an answer is not yet known.

### A. Desired consolidation outcome

#### A1. What pain are you primarily trying to remove?

Examples include duplicate song data, multiple deployments, duplicated feature work, fragmented user experience, or difficulty sharing improvements.

**Answer:** I'm involved in both communities, sometimes sourcing "songs" from one into the other and vice versa. I'm often trying to align on a single version (including lyrics, chords etc), but it's not always possible. I'm adjusting the songs sometimes to fix occasional errors/incorrect chords etc. Currently, both song sets are maintained separately; some separation is needed, but I'd like to deduplicate as much as possible.

#### A2. Do you want users to perceive one product, or would invisible technical/data consolidation be sufficient?

**Answer:** I'd probably want one product for the end-users in either community, and the other operational product for myself and collaborators.

#### A3. Is this primarily a personal efficiency project, or do you intend to offer the resulting system to additional communities?

**Answer:** Currently only for communities that I'm personally involved with. Define what would change if I'd like to expose it to the "World".

#### A4. What would make consolidation successful one year from now?

**Answer:** Having a single, extendable backend that would allow me to maintain songs selection in both communities in a flexible way.

### B. Audiences and current usage

#### B1. Who currently uses the Android app, approximately how many people use it, and how is it distributed?

**Answer:** Google Play (very limited audience, only alpha deployment for pre-defined "testers", joining has much friction now). Public deployment wanted.

#### B2. Who actively uses songs-manager today?

**Answer:** Mostly myself.

#### B3. Do all authenticated songs-manager users intentionally work on one shared Schola repertoire?

**Answer:** Not much work apart from myself.

#### B4. Could another community eventually receive its own private repertoire in the same songs-manager deployment, or would each community run a separate deployment?

**Answer:** Maybe, I haven't considered it yet.

#### B5. Are the RRN and Schola user groups completely separate, partially overlapping, or substantially the same?

**Answer:** Partially overlapping; I am the common link.

### C. RRN editorial ownership

#### C1. Is RRN 2022 an official published edition that should remain historically fixed, or a living catalog that receives ongoing corrections?

**Answer:** It is published; some corrections should be possible, but to what extent - yet to be defined.

#### C2. Who is authorized to add, remove, or correct RRN content?

**Answer:** Very limited group, we can assume myself only.

#### C3. Is there a source more authoritative than `RRN_2022.json` for RRN content?

**Answer:** No - this is a jsonized version of the songbook.

#### C4. Should corrections create a new Songbook publication, or update the existing `RRN 2022` collection in place?

**Answer:** To be discussed; fixed snapshot for 2022 might be sensible, but having new songbook for every fix is definitely too much

#### C5. Must old RRN releases remain reproducible after corrections?

**Answer:** Related to previous one, needs to be discussed.

### D. Sharing and content variants

#### D1. Should the Schola community be able to browse or import songs from RRN?

**Answer:** Maintainers probably yes, viewers no

#### D2. Should RRN maintainers be able to take corrections or improvements from Schola?

**Answer:** Maybe, to be discussed

#### D3. When RRN and Schola contain the same title, should the default assumption be that they are the same song identity?

**Answer:** Needs manual reconciliation, probably yes

#### D4. If two communities use different verses, wording, chords, keys, attributions, or presentation orders, should those remain explicit versions or should one version be canonical?

**Answer:** Explicit versions

#### D5. Which term best describes such differences in this domain: arrangement, edition, version, local variant, or something else?

**Answer:** version

#### D6. Should improvements to shared content propagate automatically, be offered as optional updates, or require manual copying?

**Answer:** No automatic propagation. Editing shared content creates a new Song Version, and the maintainer explicitly chooses which Songbook Entries and Repertoire Entries adopt it.

#### D7. Should a community be able to fork a published song version and maintain its own version permanently?

**Answer:** Yes. Only maintainers add or edit songs; a maintainer may keep a community on its own explicit Song Version.

### E. Privacy and community boundaries

#### E1. Should ratings, local notes, tags, setlists, and performance history always remain private to a community?

**Answer:** yes

#### E2. Are any Schola metadata fields useful and safe to publish to ordinary Songbook readers?

**Answer:** Needs discussion

#### E3. Should different communities be allowed to see that another community has a version of the same song?

**Answer:** probably depending on the role

#### E4. If multiple communities share a deployment, who can administer users and community membership?

**Answer:** only me for now

### F. Licensing and distribution

#### F1. Are the RRN lyrics and chords legally permitted to be redistributed through the current public API and Android app?

**Answer:** Not sure, rather yes

#### F2. May Schola's repertoire content be redistributed outside its current community?

**Answer:** Yes

#### F3. Are attribution, copyright, CCLI, access-control, or takedown requirements applicable?

**Answer:** Don't know

#### F4. Would combining the datasets under one service change any licensing or organizational agreement?

**Answer:** Don't think so

### G. Android product direction

#### G1. Is Android expected to remain a read-only browser?

**Answer:** Rather yes, might be extended to support setlists too (with their creation, depending on the role).

#### G2. Do you foresee personal favorites, personal setlists, community setlists, editing, or presentation in Android?

**Answer:** Favorites and personal/community setlists are expected. Favorites may remain device-local initially. Community setlists are maintainer-created and could potentially be created in Android as well as in the operational web app.

#### G3. Is robust offline access essential, useful, or no longer important?

**Answer:** still important

#### G4. Should Android expose several selectable songbooks, or continue opening directly into RRN?

**Answer:** Option to have several

#### G5. Should one song be allowed to appear in several songbooks in Android?

**Answer:** Yes

#### G6. Should the unfinished Android add/edit flow be removed, completed, or left deferred?

**Answer:** Removed

#### G7. Is fullscreen presentation a Schola-only workflow, or might Android readers need it too?

**Answer:** It's not essential in neither app at the moment.

### H. Publication workflow

#### H1. Should a saved editorial change become visible to Android immediately, or should publication have draft, review, and release stages?

**Answer:** immediately

#### H2. Who can publish a Songbook release?

**Answer:** maintainer only

#### H3. Should Android download an entire immutable publication snapshot or receive incremental record updates?

**Answer:** Don't know

#### H4. How quickly must a published correction reach Android users?

**Answer:** Reasonably quickly (minutes-hours are ok)

#### H5. Should users be notified when a Songbook publication changes?

**Answer:** no

#### H6. Should a publication be publicly accessible without authentication?

**Answer:** don't know yet

### I. Identity and migration

#### I1. Do existing RRN song IDs need to remain stable?

Consider bookmarks, external links, shared references, or installed-app caches.

**Answer:** Not really at the moment. We should keep in-songbook song index within the data model, to display it in the songbook context.

#### I2. Are existing songs-manager song IDs referenced by anything outside songs-manager?

**Answer:** no

#### I3. Is exact round-trip compatibility with OpenSong required for every imported field?

**Answer:** not sure, to be discussed

#### I4. Which representation should be authoritative for structured lyrics and chords: OpenSong source text, parsed sections, or both?

**Answer:** OpenSong could be a canonical source format probably, lets discuss it

#### I5. Should imports preserve the original source file as an audit artifact?

**Answer:** no

#### I6. How should likely duplicates be resolved: automatically, through a review queue, or only during explicit imports?

**Answer:** review

### J. Technical and operational constraints

#### J1. Are you comfortable making FastAPI/PostgreSQL the long-term shared backend and retiring the Kotlin/Spring backend?

**Answer:** yes

#### J2. Must the existing mobile API remain backward-compatible during migration?

**Answer:** no

#### J3. Can the Android app and backend be released together, or must older installed app versions continue working indefinitely?

**Answer:** can be released together, older versions not important

#### J4. Is the current songs-manager hosting suitable for a reader-facing API in reliability, HTTPS, backup, and traffic terms?

**Answer:** It wasn't a concern before, we should discuss it at some point - it's a VPS.

#### J5. Who will operate deployments, backups, user management, and content recovery?

**Answer:** me

#### J6. What migration risk and time horizon are reasonable?

Examples: a small consolidation over several weeks, or a larger platform investment over several months.

**Answer:** Few weeks, with AI assistance we should be moving fast.

#### J7. Are there costs or technology constraints that rule out any of the candidate options?

**Answer:** Costs should be limited, but I don't think of any specific constraints right now.

## Decisions to make after the questions are answered

The responses should allow the following decisions to be made explicitly:

1. Whether consolidation is primarily about data, backend infrastructure, or one user-facing product.
2. Whether RRN is a fixed publication or a live repertoire.
3. Whether songs-manager should become the publication authority.
4. Whether multi-community workspaces are required now, later, or never.
5. Whether shared song identity and explicit content versions are necessary.
6. Which data is public, shared, or community-private.
7. Whether Android remains a native, offline-first reader.
8. Whether to retire the Kotlin/Spring backend.
9. What migration compatibility guarantees are required.
10. Whether the next step should be a conversion prototype, a product model, or an implementation PRD.

## Suggested next discovery step

After answers are added, review them together and produce:

- a confirmed product boundary and vocabulary;
- a comparison of two or three viable target-product shapes;
- a recommended staged migration;
- explicit non-goals;
- optionally, an ADR or PRD once a hard-to-reverse direction has been chosen.
