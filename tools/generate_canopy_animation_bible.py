"""Regenerates the Animation Bible table of docs/design/canopy_breaker.md from the code (single source of truth).

Reads the enum entries of entity/canopy/CanopyAnim.java (loop flag, activating state, gameplay event, interruption, next)
and the durations declared in client/canopy/CanopyBreakerAnimations.java, then rewrites the block between the
ANIMATION-BIBLE markers of the design document.

Usage: python tools/generate_canopy_animation_bible.py
"""
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ENUM = ROOT / "src/main/java/com/codenamecoraline/entity/canopy/CanopyAnim.java"
ANIMS = ROOT / "src/main/java/com/codenamecoraline/client/canopy/CanopyBreakerAnimations.java"
DOC = ROOT / "docs/design/canopy_breaker.md"
START, END = "<!-- ANIMATION-BIBLE:START -->", "<!-- ANIMATION-BIBLE:END -->"

lengths = {}
for m in re.finditer(r"def\(CanopyAnim\.(\w+),\s*([0-9.]+)f", ANIMS.read_text(encoding="utf-8")):
    lengths[m.group(1)] = float(m.group(2))

groups = []
rows = []
group = None
entry = re.compile(r'^\s*(\w+)\((true|false),\s*"([^"]*)",\s*"([^"]*)",\s*"([^"]*)",\s*"([^"]*)"\)')
for line in ENUM.read_text(encoding="utf-8").splitlines():
    g = re.match(r"^\s*// ---- (.+)$", line)
    if g:
        group = g.group(1).strip()
        rows.append(("group", group))
        continue
    m = entry.match(line)
    if m:
        name, loops, state, event, interrupt, nxt = m.groups()
        rows.append(("anim", name, loops == "true", state, event, interrupt, nxt))

missing = [r[1] for r in rows if r[0] == "anim" and r[1] not in lengths]
if missing:
    raise SystemExit("animations without keyframes: " + ", ".join(missing))

out = [START, "",
       "Gerada por `tools/generate_canopy_animation_bible.py` a partir de `CanopyAnim` e `CanopyBreakerAnimations`; "
       "não edite à mão. As colunas de estado, evento, interrupção e próxima seguem o texto do enum (inglês).", ""]
count = 0
for r in rows:
    if r[0] == "group":
        out += ["", f"#### {r[1]}", "",
                "| Animação | Loop | Duração (s) | Estado que ativa | Evento de gameplay | Interrupção | Próxima |",
                "|---|---|---|---|---|---|---|"]
    else:
        _, name, loops, state, event, interrupt, nxt = r
        count += 1
        out.append(f"| `{name.lower()}` | {'sim' if loops else 'não'} | {lengths[name]:.2f} | {state} | {event} | {interrupt} | {nxt} |")
out += ["", f"Total: **{count} animações**, todas com keyframes definidos.", "", END]
block = "\n".join(out)

text = DOC.read_text(encoding="utf-8")
if START in text and END in text:
    text = text[:text.index(START)] + block + text[text.index(END) + len(END):]
else:
    raise SystemExit("markers not found in " + str(DOC))
DOC.write_text(text, encoding="utf-8", newline="\n")
print(f"Animation Bible updated: {count} animations.")
