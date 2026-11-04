"""Regenerates the animation table of docs/design/crimson_commander.md from the code (single source of truth).

Reads the enum entries of entity/crimson/CrimsonAnim.java (loop flag, activating state, gameplay event, interruption,
next) and the durations declared in client/crimson/CrimsonCommanderAnimations.java, then rewrites the block between the
ANIMATION-TABLE markers of the design document.

Usage: python tools/generate_crimson_animation_table.py
"""
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ENUM = ROOT / "src/main/java/com/codenamecoraline/entity/crimson/CrimsonAnim.java"
ANIMS = ROOT / "src/main/java/com/codenamecoraline/client/crimson/CrimsonCommanderAnimations.java"
DOC = ROOT / "docs/design/crimson_commander.md"
START, END = "<!-- ANIMATION-TABLE:START -->", "<!-- ANIMATION-TABLE:END -->"

lengths = {m.group(1): float(m.group(2))
           for m in re.finditer(r"def\(CrimsonAnim\.(\w+),\s*([0-9.]+)f", ANIMS.read_text(encoding="utf-8"))}
rows = []
entry = re.compile(r'^\s*(\w+)\((true|false),\s*"([^"]*)",\s*"([^"]*)",\s*"([^"]*)",\s*"([^"]*)"\)')
for line in ENUM.read_text(encoding="utf-8").splitlines():
    g = re.match(r"^\s*// ---- (.+)$", line)
    if g:
        rows.append(("group", g.group(1).strip()))
        continue
    m = entry.match(line)
    if m:
        rows.append(("anim", *m.groups()))

missing = [r[1] for r in rows if r[0] == "anim" and r[1] not in lengths]
if missing:
    raise SystemExit("animations without keyframes: " + ", ".join(missing))

count = sum(1 for r in rows if r[0] == "anim")
out = [START, "", f"{count} animações (`CrimsonAnim`), todas com keyframes em `CrimsonCommanderAnimations`.", ""]
for r in rows:
    if r[0] == "group":
        out += ["", f"**{r[1]}**", "", "| Animação | Duração | Laço | Estado que ativa | Evento | Interrupção | Próxima |",
                "|---|---|---|---|---|---|---|"]
        continue
    _, name, loops, state, event, interrupt, nxt = r
    out.append(f"| `{name.lower()}` | {lengths[name]:.2f} s | {'sim' if loops == 'true' else 'não'} | {state} | {event} | "
               f"{interrupt} | {nxt} |")
out += ["", END]

doc = DOC.read_text(encoding="utf-8")
a, b = doc.index(START), doc.index(END) + len(END)
DOC.write_text(doc[:a] + "\n".join(out) + doc[b:], encoding="utf-8", newline="\n")
print(f"Wrote the table of {count} animations into {DOC.relative_to(ROOT)}")
