"""Construit la banque de versets embarquée dans l'app à partir de data/themes_source.txt et d'un export LSG 1910 (getbible v2).

Usage : python3 tools/build_verses.py <ls1910.json>
"""
import json, re, sys, unicodedata
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
REF_RE = re.compile(r"^(.+?) (\d+):(\d+)(?:-(\d+))?$")
MAX_WORDS = 50
OUTPUT = ROOT / "shared/src/commonMain/composeResources/files/verses.json"
BOOK_ALIASES = {"Actes": "Actes des Apôtres"}


def clean(text):
    text = text.replace(" ", "").replace("\xa0", " ")
    text = re.sub(r"\s+([,.])", r"\1", text)
    text = re.sub(r", -(?=\w)", ", ", text)
    return re.sub(r"\s+", " ", text).strip()


PSALM_TITLE_RE = re.compile(r"^(?:(?:Cantique des degrés|Cantique de David|Psaume de David|De David|De Salomon|Cantique|Psaume)\. )+")
PAUSE_RE = re.compile(r"\s*-?\s*Pause\.?")
CHOIR_NOTE_RE = re.compile(r"\s*Au chef des chantres\..*$")


def strip_annotations(text, book, verse):
    """Retire les titres de psaumes (numérotés au verset 1 dans la LSG) et les « Pause » (Sélah)."""
    if book == "Psaumes" and verse == 1:
        text = PSALM_TITLE_RE.sub("", text)
    return CHOIR_NOTE_RE.sub("", PAUSE_RE.sub("", text))


def finish(text):
    """Rend autonome un verset coupé en milieu de phrase : majuscule initiale, ponctuation finale."""
    text = text[0].upper() + text[1:]
    text = re.sub(r"[,;: \s]+$", "", text)
    if not re.search(r"[.!?…»]$", text):
        text += "."
    return text


def slug(s):
    s = unicodedata.normalize("NFD", s).encode("ascii", "ignore").decode().lower()
    return re.sub(r"[^a-z0-9]+", "-", s).strip("-")


def difficulty(n):
    return "facile" if n <= 20 else "moyen" if n <= 35 else "difficile"


def main(lsg_path):
    bible = json.load(open(lsg_path, encoding="utf-8"))
    books = {b["name"]: {c["chapter"]: {v["verse"]: v["text"] for v in c["verses"]} for c in b["chapters"]}
             for b in bible["books"]}

    themes, verses, errors = [], {}, []
    for line in (ROOT / "data/themes_source.txt").read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if not line or line.startswith("//"):
            continue
        if line.startswith("#"):
            tid, name, family = (p.strip() for p in line[1:].split("|"))
            themes.append({"id": tid, "name": name, "family": family, "verseIds": []})
            continue
        ref, ctx = (p.strip() for p in line.split("|", 1))
        m = REF_RE.match(ref)
        if not m:
            errors.append(f"référence illisible : {ref}")
            continue
        book, ch, v1 = BOOK_ALIASES.get(m.group(1), m.group(1)), int(m.group(2)), int(m.group(3))
        v2 = int(m.group(4) or v1)
        vid = slug(ref)
        if vid not in verses:
            try:
                text = finish(" ".join(strip_annotations(clean(books[book][ch][v]), book, v) for v in range(v1, v2 + 1)))
            except KeyError:
                errors.append(f"introuvable dans la LSG : {ref}")
                continue
            n = len(text.split())
            verses[vid] = {"id": vid, "reference": ref, "book": book, "chapter": ch,
                           "verseStart": v1, "verseEnd": v2, "text": text, "context": ctx,
                           "wordCount": n, "difficulty": difficulty(n), "themes": []}
        elif ctx and not verses[vid]["context"]:
            verses[vid]["context"] = ctx
        verses[vid]["themes"].append(themes[-1]["id"])
        themes[-1]["verseIds"].append(vid)

    errors += [f"sans contexte : {v['reference']}" for v in verses.values() if not v["context"]]
    errors += [f"trop long ({v['wordCount']} mots) : {v['reference']}" for v in verses.values() if v["wordCount"] > MAX_WORDS]
    if errors:
        sys.exit("\n".join(errors))

    out = {"translation": "Louis Segond 1910", "source": "api.getbible.net/v2/ls1910",
           "themes": themes, "verses": list(verses.values())}
    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    OUTPUT.write_text(json.dumps(out, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"{len(themes)} thèmes, {len(verses)} versets uniques")


if __name__ == "__main__":
    main(sys.argv[1])
