from pathlib import Path

from PIL import Image


ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / "images" / "人物抠图_144x144.png"
OUT = ROOT / "images" / "人物抠图_80x80.png"


img = Image.open(SRC).convert("RGBA")
img = img.resize((80, 80), Image.Resampling.LANCZOS)
img.save(OUT)
print(OUT)
print(img.size)
