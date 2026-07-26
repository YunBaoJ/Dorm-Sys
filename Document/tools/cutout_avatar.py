from collections import deque
from pathlib import Path

import numpy as np
from PIL import Image, ImageFilter


SRC = Path(r"D:/Administrator/桌面/微信图片_20260720220411_8826_3.jpg")
OUT = Path(r"F:/bishe/Antigravity/Document/images/人物抠图_144x144.png")


def main():
    OUT.parent.mkdir(parents=True, exist_ok=True)

    img = Image.open(SRC).convert("RGBA")
    arr = np.array(img)
    rgb = arr[:, :, :3].astype(np.int32)
    h, w = rgb.shape[:2]

    border = np.concatenate([rgb[0, :, :], rgb[-1, :, :], rgb[:, 0, :], rgb[:, -1, :]], axis=0)
    bg = np.median(border, axis=0).astype(np.int32)

    dist = np.sqrt(((rgb - bg) ** 2).sum(axis=2))
    candidate = dist < 58
    visited = np.zeros((h, w), dtype=bool)
    q = deque()

    for x in range(w):
        if candidate[0, x]:
            q.append((0, x))
            visited[0, x] = True
        if candidate[h - 1, x]:
            q.append((h - 1, x))
            visited[h - 1, x] = True
    for y in range(h):
        if candidate[y, 0] and not visited[y, 0]:
            q.append((y, 0))
            visited[y, 0] = True
        if candidate[y, w - 1] and not visited[y, w - 1]:
            q.append((y, w - 1))
            visited[y, w - 1] = True

    while q:
        y, x = q.popleft()
        for ny, nx in ((y - 1, x), (y + 1, x), (y, x - 1), (y, x + 1)):
            if 0 <= ny < h and 0 <= nx < w and candidate[ny, nx] and not visited[ny, nx]:
                visited[ny, nx] = True
                q.append((ny, nx))

    alpha = (~visited).astype(np.uint8) * 255
    alpha_img = Image.fromarray(alpha, "L")
    alpha_img = (
        alpha_img
        .filter(ImageFilter.MaxFilter(3))
        .filter(ImageFilter.MinFilter(3))
        .filter(ImageFilter.GaussianBlur(0.45))
    )

    cut = img.copy()
    cut.putalpha(alpha_img)

    box = cut.getbbox()
    if box is None:
        raise RuntimeError("No foreground detected")

    cut = cut.crop(box)
    size = 144
    padding = 8
    scale = min((size - padding * 2) / cut.width, (size - padding * 2) / cut.height)
    new_size = (max(1, round(cut.width * scale)), max(1, round(cut.height * scale)))
    cut = cut.resize(new_size, Image.Resampling.LANCZOS)

    canvas = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    canvas.alpha_composite(cut, ((size - new_size[0]) // 2, (size - new_size[1]) // 2))
    canvas.save(OUT)
    print(OUT)
    print(canvas.size)


if __name__ == "__main__":
    main()
