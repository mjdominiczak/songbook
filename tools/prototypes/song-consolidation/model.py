"""PROTOTYPE: pure canonical-song conversion and comparison logic."""

from __future__ import annotations

from dataclasses import asdict, dataclass
from difflib import SequenceMatcher, unified_diff
from itertools import zip_longest
import re
import unicodedata


@dataclass(frozen=True)
class SongLine:
    text: str
    chords: str | None = None


@dataclass(frozen=True)
class SongSection:
    key: str
    kind: str
    number: int | None
    lines: tuple[SongLine, ...]


@dataclass(frozen=True)
class CanonicalSongVersion:
    title: str
    alternate_title: str | None
    attribution: str | None
    notes: str | None
    musical_key: str | None
    time_signature: str | None
    presentation: tuple[str, ...]
    sections: tuple[SongSection, ...]
    source: str

    def visible_state(self) -> dict:
        return asdict(self)


@dataclass(frozen=True)
class Comparison:
    title: str
    lyric_similarity: float
    chord_similarity: float
    rrn_sections: int
    schola_sections: int
    classification: str
    risks: tuple[str, ...]


def from_rrn(record: dict) -> CanonicalSongVersion:
    sections: list[SongSection] = []
    presentation: list[str] = []

    for index, raw in enumerate(record.get("content") or [], start=1):
        number = raw.get("number")
        if number is None:
            kind, key = "simple", f"S{index}"
        elif number == 0:
            kind, key = "chorus", "C"
        else:
            kind, key = "verse", f"V{number}"

        text_lines = (raw.get("text") or "").splitlines()
        chord_value = raw.get("chords")
        chord_lines = chord_value.splitlines() if chord_value is not None else []
        lines = tuple(
            SongLine(text=text or "", chords=chords if chords not in (None, "") else None)
            for text, chords in zip_longest(text_lines, chord_lines, fillvalue=None)
        )
        sections.append(SongSection(key=key, kind=kind, number=number, lines=lines))
        presentation.append(key)

    return CanonicalSongVersion(
        title=record["title"],
        alternate_title=record.get("titleAlt"),
        attribution=None,
        notes=record.get("info"),
        musical_key=None,
        time_signature=None,
        presentation=tuple(presentation),
        sections=tuple(sections),
        source="rrn-json",
    )


def from_opensong_record(record: dict) -> CanonicalSongVersion:
    sections: list[SongSection] = []
    current_key = "S1"
    current_lines: list[SongLine] = []
    pending_chords: str | None = None
    unnamed_index = 1

    def flush() -> None:
        nonlocal current_lines, pending_chords
        if pending_chords is not None:
            current_lines.append(SongLine(text="", chords=pending_chords))
            pending_chords = None
        if not current_lines:
            return
        kind, number = section_identity(current_key)
        sections.append(
            SongSection(
                key=current_key,
                kind=kind,
                number=number,
                lines=tuple(current_lines),
            )
        )
        current_lines = []

    for raw_line in (record.get("lyrics") or "").splitlines():
        header = re.fullmatch(r"\s*\[([^]]+)]\s*", raw_line)
        if header:
            flush()
            current_key = normalize_section_key(header.group(1))
            continue
        if raw_line.startswith("."):
            if pending_chords is not None:
                current_lines.append(SongLine(text="", chords=pending_chords))
            pending_chords = raw_line[1:]
            continue
        if not raw_line.strip():
            continue
        current_lines.append(SongLine(text=raw_line, chords=pending_chords))
        pending_chords = None

    flush()

    if not sections and record.get("lyrics"):
        unnamed_index += 1

    presentation = tuple(
        normalize_section_key(token)
        for token in (record.get("presentation") or "").split()
    )
    if not presentation:
        presentation = tuple(section.key for section in sections)

    return CanonicalSongVersion(
        title=record["title"],
        alternate_title=record.get("aka"),
        attribution=record.get("author"),
        notes=record.get("notes"),
        musical_key=record.get("key"),
        time_signature=record.get("time_sig"),
        presentation=presentation,
        sections=tuple(sections),
        source="opensong",
    )


def normalize_section_key(value: str) -> str:
    compact = re.sub(r"\s+", "", value).upper()
    aliases = {
        "CHORUS": "C",
        "REFRAIN": "C",
        "BRIDGE": "B",
        "PRECHORUS": "PC",
    }
    if compact in aliases:
        return aliases[compact]
    verse = re.fullmatch(r"VERSE(\d+)", compact)
    if verse:
        return f"V{verse.group(1)}"
    chorus = re.fullmatch(r"CHORUS(\d+)", compact)
    if chorus:
        return f"C{chorus.group(1)}"
    return compact


def section_identity(key: str) -> tuple[str, int | None]:
    verse = re.fullmatch(r"V(\d+)", key)
    if verse:
        return "verse", int(verse.group(1))
    chorus = re.fullmatch(r"C(\d*)", key)
    if chorus:
        return "chorus", int(chorus.group(1) or 0)
    if key.startswith("S"):
        return "simple", None
    return "named", None


def compare_versions(rrn: CanonicalSongVersion, schola: CanonicalSongVersion) -> Comparison:
    lyric_similarity = similarity(lyric_tokens(rrn), lyric_tokens(schola))
    chord_similarity = similarity(chord_tokens(rrn), chord_tokens(schola))
    risks: list[str] = []

    if len(rrn.sections) != len(schola.sections):
        risks.append("section-count mismatch")
    if rrn.presentation != schola.presentation:
        risks.append("presentation order differs or is absent in RRN")
    if rrn.notes and (schola.attribution or schola.time_signature):
        risks.append("RRN info needs metadata reconciliation")
    if lyric_similarity < 0.98:
        risks.append("lyrics differ")
    if chord_similarity < 0.95:
        risks.append("chords differ")

    if lyric_similarity >= 0.98 and chord_similarity >= 0.95:
        classification = "shared-version candidate"
    elif lyric_similarity >= 0.80:
        classification = "explicit-version candidate"
    else:
        classification = "manual identity review"

    return Comparison(
        title=rrn.title,
        lyric_similarity=lyric_similarity,
        chord_similarity=chord_similarity,
        rrn_sections=len(rrn.sections),
        schola_sections=len(schola.sections),
        classification=classification,
        risks=tuple(risks),
    )


def lyric_tokens(song: CanonicalSongVersion) -> tuple[str, ...]:
    return normalized_tokens(
        (line.text for section in song.sections for line in section.lines),
        pattern=r"[a-z0-9]+",
    )


def chord_tokens(song: CanonicalSongVersion) -> tuple[str, ...]:
    return normalized_tokens(
        (line.chords or "" for section in song.sections for line in section.lines),
        pattern=r"[a-z0-9+#/]+",
    )


def normalized_tokens(values, pattern: str) -> tuple[str, ...]:
    text = " ".join(values).lower()
    decomposed = unicodedata.normalize("NFKD", text)
    without_marks = "".join(char for char in decomposed if not unicodedata.combining(char))
    return tuple(re.findall(pattern, without_marks))


def similarity(left: tuple[str, ...], right: tuple[str, ...]) -> float:
    if not left and not right:
        return 1.0
    if not left or not right:
        return 0.0
    return round(SequenceMatcher(None, left, right).ratio(), 3)


def diff_song_versions(
    rrn: CanonicalSongVersion,
    schola: CanonicalSongVersion,
    kind: str,
) -> tuple[str, ...]:
    """Return a source-labelled, display-ready diff for one aspect of a song."""
    projections = {
        "lyrics": lyric_projection,
        "chords": chord_projection,
        "metadata": metadata_projection,
    }
    projection = projections[kind]
    left = projection(rrn)
    right = projection(schola)
    if left == right:
        return ("  No differences.",)
    return tuple(
        unified_diff(
            left,
            right,
            fromfile=f"RRN/{kind}",
            tofile=f"Schola/{kind}",
            lineterm="",
            n=2,
        )
    )


def lyric_projection(song: CanonicalSongVersion) -> tuple[str, ...]:
    """Semantic lyrics: preserve wording/punctuation but ignore source wrapping."""
    result: list[str] = []
    for section in song.sections:
        result.append(f"[{section.key}]")
        result.append(
            " ".join(line.text.strip() for line in section.lines if line.text.strip())
        )
    return tuple(result)


def chord_projection(song: CanonicalSongVersion) -> tuple[str, ...]:
    """Chord lines with spaces made visible so alignment changes can be assessed."""
    result: list[str] = []
    for section in song.sections:
        result.append(f"[{section.key}]")
        for index, line in enumerate(section.lines, start=1):
            chords = visible_whitespace(line.chords) if line.chords else "<none>"
            lyric = line.text.strip()
            if len(lyric) > 42:
                lyric = f"{lyric[:39]}..."
            result.append(f"{index:02}: {chords}  | {lyric}")
    return tuple(result)


def metadata_projection(song: CanonicalSongVersion) -> tuple[str, ...]:
    fields = (
        ("title", song.title),
        ("alternate_title", song.alternate_title),
        ("attribution", song.attribution),
        ("notes", song.notes),
        ("musical_key", song.musical_key),
        ("time_signature", song.time_signature),
        ("presentation", " ".join(song.presentation)),
    )
    return tuple(f"{name}: {display_value(value)}" for name, value in fields)


def visible_whitespace(value: str) -> str:
    return value.replace("\t", "→   ").replace(" ", "·")


def display_value(value: str | None) -> str:
    if value is None or value == "":
        return "<none>"
    return value.replace("\r", "").replace("\n", "\\n")
