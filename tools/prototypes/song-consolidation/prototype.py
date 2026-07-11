"""PROTOTYPE TUI for comparing real RRN and songs-manager song records."""

from __future__ import annotations

import argparse
import json
from pathlib import Path
import sqlite3
import sys

from model import (
    Comparison,
    compare_versions,
    diff_song_versions,
    from_opensong_record,
    from_rrn,
)


BOLD = "\x1b[1m"
DIM = "\x1b[2m"
RESET = "\x1b[0m"
PAGE_SIZE = 16
DETAIL_MODES = ("lyrics", "chords", "metadata", "sections", "canonical")


def load_state(manager_db_override: str | None = None) -> list[dict]:
    mobile_root = Path(__file__).resolve().parents[3]
    rrn_path = mobile_root.parent / "backend" / "src" / "main" / "resources" / "static" / "RRN_2022.json"
    manager_db = (
        Path(manager_db_override).resolve()
        if manager_db_override
        else mobile_root.parent.parent / "songs-manager" / "backend" / "songs_manager.db"
    )

    rrn_records = json.loads(rrn_path.read_text(encoding="utf-8"))
    with sqlite3.connect(f"{manager_db.as_uri()}?mode=ro", uri=True) as connection:
        connection.row_factory = sqlite3.Row
        manager_records = {
            row["title"].casefold(): dict(row)
            for row in connection.execute(
                """
                SELECT title, lyrics, author, aka, key, time_sig,
                       presentation, notes
                FROM songs
                ORDER BY title
                """
            )
        }

    overlaps: list[dict] = []
    for raw_rrn in rrn_records:
        raw_manager = manager_records.get(raw_rrn["title"].casefold())
        if raw_manager is None:
            continue
        rrn = from_rrn(raw_rrn)
        schola = from_opensong_record(raw_manager)
        overlaps.append(
            {
                "comparison": compare_versions(rrn, schola),
                "rrn": rrn,
                "schola": schola,
                "decision": "unresolved",
            }
        )
    return sorted(overlaps, key=lambda item: item["comparison"].title.casefold())


def render(state: list[dict], index: int, detail: str, page: int) -> int:
    print("\x1b[2J\x1b[H", end="")
    current = state[index]
    comparison: Comparison = current["comparison"]
    counts = classification_counts(state)

    print(f"{BOLD}PROTOTYPE — canonical Song Version reconciliation{RESET}")
    print(f"{DIM}Question: can structured sections preserve both sources and expose sharing risks?{RESET}\n")
    print(f"{BOLD}Overlap{RESET}: {index + 1}/{len(state)}  {BOLD}Title{RESET}: {comparison.title}")
    print(f"{BOLD}Suggested{RESET}: {comparison.classification}")
    print(f"{BOLD}Decision{RESET}: {current['decision']}")
    print(f"{BOLD}Lyric similarity{RESET}: {comparison.lyric_similarity:.3f}")
    print(f"{BOLD}Chord similarity{RESET}: {comparison.chord_similarity:.3f}")
    print(
        f"{BOLD}Chord lines{RESET}: "
        f"RRN={chord_line_count(current['rrn'])}, "
        f"Schola={chord_line_count(current['schola'])}"
    )
    print(f"{BOLD}Sections{RESET}: RRN={comparison.rrn_sections}, Schola={comparison.schola_sections}")
    print(f"{BOLD}Risks{RESET}: {', '.join(comparison.risks) or 'none detected'}")
    print(
        f"{BOLD}Session decisions{RESET}: shared={counts['shared']}, "
        f"variant={counts['variant']}, unresolved={counts['unresolved']}"
    )

    lines = detail_lines(current, detail)
    page_count = max(1, (len(lines) + PAGE_SIZE - 1) // PAGE_SIZE)
    page = min(page, page_count - 1)
    start = page * PAGE_SIZE
    print(f"\n{BOLD}{detail.upper()} DIFF{RESET}  {DIM}page {page + 1}/{page_count}{RESET}")
    print("\n".join(lines[start : start + PAGE_SIZE]))

    print(
        f"\n{BOLD}[n]{RESET} next  {BOLD}[p]{RESET} previous  "
        f"{BOLD}[s]{RESET} mark shared  {BOLD}[v]{RESET} mark variant  "
        f"{BOLD}[u]{RESET} unresolved  {BOLD}[d]{RESET} next diff  "
        f"{BOLD}[j/k]{RESET} diff page  "
        f"{BOLD}[q]{RESET} quit"
    )
    return page_count


def section_summary(song) -> list[str]:
    rows = []
    for section in song.sections:
        chord_lines = sum(1 for line in section.lines if line.chords)
        rows.append(
            f"  {section.key:<5} {section.kind:<7} "
            f"lines={len(section.lines):<2} chord-lines={chord_lines}"
        )
    return rows or ["  (none)"]


def detail_lines(current: dict, detail: str) -> list[str]:
    if detail in ("lyrics", "chords", "metadata"):
        return list(diff_song_versions(current["rrn"], current["schola"], detail))
    if detail == "sections":
        return (
            ["--- RRN/sections"]
            + section_summary(current["rrn"])
            + ["+++ Schola/sections"]
            + section_summary(current["schola"])
        )
    return (
        ["--- RRN/canonical"]
        + json.dumps(
            current["rrn"].visible_state(), ensure_ascii=False, indent=2
        ).splitlines()
        + ["+++ Schola/canonical"]
        + json.dumps(
            current["schola"].visible_state(), ensure_ascii=False, indent=2
        ).splitlines()
    )


def classification_counts(state: list[dict]) -> dict[str, int]:
    return {
        value: sum(1 for item in state if item["decision"] == value)
        for value in ("shared", "variant", "unresolved")
    }


def print_summary(state: list[dict]) -> None:
    suggestions: dict[str, int] = {}
    risk_counts: dict[str, int] = {}
    for item in state:
        comparison: Comparison = item["comparison"]
        suggestions[comparison.classification] = suggestions.get(comparison.classification, 0) + 1
        for risk in comparison.risks:
            risk_counts[risk] = risk_counts.get(risk, 0) + 1

    print("PROTOTYPE — song consolidation batch summary")
    print(f"Exact-title overlaps: {len(state)}")
    print(
        "Overlap records containing chords: "
        f"RRN={sum(has_chords(item['rrn']) for item in state)}, "
        f"Schola={sum(has_chords(item['schola']) for item in state)}"
    )
    for name, count in sorted(suggestions.items()):
        print(f"  {name}: {count}")
    print("Conversion/reconciliation risks:")
    for name, count in sorted(risk_counts.items(), key=lambda item: (-item[1], item[0])):
        print(f"  {name}: {count}")
    print("\nLowest lyric similarity:")
    for item in sorted(state, key=lambda value: value["comparison"].lyric_similarity)[:8]:
        comparison = item["comparison"]
        print(
            f"  {comparison.lyric_similarity:.3f} lyrics / "
            f"{comparison.chord_similarity:.3f} chords  {comparison.title}"
        )
    print("\nHighest chord similarity:")
    for item in sorted(
        state,
        key=lambda value: value["comparison"].chord_similarity,
        reverse=True,
    )[:8]:
        comparison = item["comparison"]
        print(
            f"  {comparison.chord_similarity:.3f} chords / "
            f"{comparison.lyric_similarity:.3f} lyrics  {comparison.title}"
        )


def has_chords(song) -> bool:
    return any(
        line.chords and line.chords.strip()
        for section in song.sections
        for line in section.lines
    )


def chord_line_count(song) -> int:
    return sum(
        1
        for section in song.sections
        for line in section.lines
        if line.chords and line.chords.strip()
    )


def find_title(state: list[dict], requested: str) -> dict:
    exact = [
        item
        for item in state
        if item["comparison"].title.casefold() == requested.casefold()
    ]
    if exact:
        return exact[0]
    partial = [
        item
        for item in state
        if requested.casefold() in item["comparison"].title.casefold()
    ]
    if len(partial) == 1:
        return partial[0]
    matches = ", ".join(item["comparison"].title for item in partial) or "none"
    raise SystemExit(f"Title must identify one exact-title overlap. Matches: {matches}")


def print_title_diff(item: dict, kind: str) -> None:
    comparison: Comparison = item["comparison"]
    print(f"{comparison.title}")
    print(
        f"lyrics={comparison.lyric_similarity:.3f}, "
        f"chords={comparison.chord_similarity:.3f}, "
        f"suggested={comparison.classification}"
    )
    print(
        f"chord lines: RRN={chord_line_count(item['rrn'])}, "
        f"Schola={chord_line_count(item['schola'])}"
    )
    kinds = ("lyrics", "chords", "metadata", "sections") if kind == "all" else (kind,)
    for current_kind in kinds:
        print(f"\n=== {current_kind.upper()} ===")
        print("\n".join(detail_lines(item, current_kind)))


def main() -> None:
    if hasattr(sys.stdout, "reconfigure"):
        sys.stdout.reconfigure(encoding="utf-8")
    parser = argparse.ArgumentParser()
    parser.add_argument("--summary", action="store_true")
    parser.add_argument("--title", help="print the detailed diff for one overlap")
    parser.add_argument(
        "--manager-db",
        help="override the songs-manager SQLite path (useful in restricted shells)",
    )
    parser.add_argument(
        "--diff-kind",
        choices=("all", "lyrics", "chords", "metadata", "sections"),
        default="all",
    )
    args = parser.parse_args()
    state = load_state(args.manager_db)

    if args.summary:
        print_summary(state)
        return
    if args.title:
        print_title_diff(find_title(state, args.title), args.diff_kind)
        return

    index = 0
    detail_index = 0
    page = 0
    while True:
        page_count = render(state, index, DETAIL_MODES[detail_index], page)
        command = input("> ").strip().lower()
        if command == "q":
            break
        if command == "n":
            index = (index + 1) % len(state)
            page = 0
        elif command == "p":
            index = (index - 1) % len(state)
            page = 0
        elif command == "s":
            state[index]["decision"] = "shared"
        elif command == "v":
            state[index]["decision"] = "variant"
        elif command == "u":
            state[index]["decision"] = "unresolved"
        elif command == "d":
            detail_index = (detail_index + 1) % len(DETAIL_MODES)
            page = 0
        elif command == "j":
            page = min(page + 1, page_count - 1)
        elif command == "k":
            page = max(page - 1, 0)


if __name__ == "__main__":
    main()
