from pathlib import Path
import math

from PIL import Image, ImageDraw, ImageFont


ROOT = Path(__file__).resolve().parents[1]
OUT_DIR = ROOT / "images"
FONT_DIR = Path("C:/Windows/Fonts")


def font(name: str, size: int) -> ImageFont.FreeTypeFont:
    return ImageFont.truetype(str(FONT_DIR / name), size=size)


F_TITLE = font("simhei.ttf", 58)
F_SUBTITLE = font("simhei.ttf", 34)
F_BODY = font("simhei.ttf", 30)
F_BODY_SMALL = font("simhei.ttf", 25)
F_NOTE = font("simhei.ttf", 22)
F_EN = font("arial.ttf", 22)


BLUE = "#1f5f9f"
BLUE_2 = "#2f80c9"
BLUE_LIGHT = "#eaf4ff"
GREEN = "#2f855a"
GREEN_LIGHT = "#eaf8ef"
ORANGE = "#b66a15"
ORANGE_LIGHT = "#fff2df"
PURPLE = "#7251b5"
PURPLE_LIGHT = "#f1edff"
GRAY = "#4a5568"
LINE = "#2d3748"
BG = "#ffffff"


def draw_center_text(draw, box, text, fnt, fill="#1a202c", line_gap=8):
    x1, y1, x2, y2 = box
    lines = text.split("\n")
    heights = []
    widths = []
    for line in lines:
        bb = draw.textbbox((0, 0), line, font=fnt)
        widths.append(bb[2] - bb[0])
        heights.append(bb[3] - bb[1])
    total_h = sum(heights) + line_gap * (len(lines) - 1)
    y = y1 + (y2 - y1 - total_h) / 2
    for line, w, h in zip(lines, widths, heights):
        draw.text((x1 + (x2 - x1 - w) / 2, y), line, font=fnt, fill=fill)
        y += h + line_gap


def rounded_box(draw, box, fill, outline, width=3, radius=22):
    draw.rounded_rectangle(box, radius=radius, fill=fill, outline=outline, width=width)


def arrow(draw, start, end, fill=LINE, width=5, label=None, label_pos=0.5, label_offset=(0, 0)):
    x1, y1 = start
    x2, y2 = end
    draw.line((x1, y1, x2, y2), fill=fill, width=width)
    ang = math.atan2(y2 - y1, x2 - x1)
    head = 18
    p1 = (x2, y2)
    p2 = (x2 - head * math.cos(ang - math.pi / 7), y2 - head * math.sin(ang - math.pi / 7))
    p3 = (x2 - head * math.cos(ang + math.pi / 7), y2 - head * math.sin(ang + math.pi / 7))
    draw.polygon((p1, p2, p3), fill=fill)
    if label:
        lx = x1 + (x2 - x1) * label_pos + label_offset[0]
        ly = y1 + (y2 - y1) * label_pos + label_offset[1]
        bb = draw.textbbox((0, 0), label, font=F_NOTE)
        pad_x, pad_y = 14, 7
        draw.rounded_rectangle(
            (lx - pad_x, ly - pad_y, lx + bb[2] - bb[0] + pad_x, ly + bb[3] - bb[1] + pad_y),
            radius=12,
            fill="#ffffff",
            outline="#cbd5e0",
            width=2,
        )
        draw.text((lx, ly), label, font=F_NOTE, fill=GRAY)


def shadow(draw, box, radius=24):
    x1, y1, x2, y2 = box
    draw.rounded_rectangle((x1 + 8, y1 + 8, x2 + 8, y2 + 8), radius=radius, fill="#d9e4f2")


def save_canvas(img, path):
    OUT_DIR.mkdir(exist_ok=True)
    img.save(path, "PNG", optimize=True)


def draw_flow_chart():
    w, h = 1600, 850
    img = Image.new("RGB", (w, h), BG)
    draw = ImageDraw.Draw(img)

    def compact_box(box, text, fnt=F_BODY):
        x1, y1, x2, y2 = box
        draw.rounded_rectangle((x1 + 8, y1 + 8, x2 + 8, y2 + 8), radius=8, fill="#d8e7f5")
        draw.rounded_rectangle(box, radius=8, fill="#eef7ff", outline="#8faec8", width=2)
        draw_center_text(draw, box, text, fnt, fill="#000000")

    center_x = w // 2
    step1 = (660, 45, 940, 115)
    step2 = (600, 180, 1000, 250)
    front = (360, 345, 700, 415)
    back = (850, 345, 1240, 415)
    step5 = (700, 495, 900, 565)
    step6 = (700, 630, 900, 700)
    step7 = (700, 765, 900, 835)

    compact_box(step1, "1.需求分析与设计")
    compact_box(step2, "2.系统架构设计与技术选型")
    compact_box(front, "3.前端开发（Vue3）")
    compact_box(back, "4.后端开发（SpringBoot）")
    compact_box(step5, "5.系统集成")
    compact_box(step6, "6.系统测试")
    compact_box(step7, "7.本地部署")

    arrow(draw, (center_x, step1[3]), (center_x, step2[1]), fill="#1b2f4a", width=3)
    branch_y = 305
    draw.line((center_x, step2[3], center_x, branch_y), fill="#1b2f4a", width=3)
    draw.line((center_x, branch_y, (front[0] + front[2]) // 2, branch_y), fill="#1b2f4a", width=3)
    draw.line((center_x, branch_y, (back[0] + back[2]) // 2, branch_y), fill="#1b2f4a", width=3)
    arrow(draw, ((front[0] + front[2]) // 2, branch_y), ((front[0] + front[2]) // 2, front[1]), fill="#1b2f4a", width=3)
    arrow(draw, ((back[0] + back[2]) // 2, branch_y), ((back[0] + back[2]) // 2, back[1]), fill="#1b2f4a", width=3)

    merge_y = 465
    draw.line(((front[0] + front[2]) // 2, front[3], (front[0] + front[2]) // 2, merge_y), fill="#1b2f4a", width=3)
    draw.line(((back[0] + back[2]) // 2, back[3], (back[0] + back[2]) // 2, merge_y), fill="#1b2f4a", width=3)
    draw.line(((front[0] + front[2]) // 2, merge_y, center_x, merge_y), fill="#1b2f4a", width=3)
    draw.line(((back[0] + back[2]) // 2, merge_y, center_x, merge_y), fill="#1b2f4a", width=3)
    arrow(draw, (center_x, merge_y), (center_x, step5[1]), fill="#1b2f4a", width=3)
    arrow(draw, (center_x, step5[3]), (center_x, step6[1]), fill="#1b2f4a", width=3)
    arrow(draw, (center_x, step6[3]), (center_x, step7[1]), fill="#1b2f4a", width=3)
    save_canvas(img, OUT_DIR / "图1-开发与部署流程.png")


def module_row(draw, x, y, labels, cell_w, cell_h, fill, outline):
    for i, label in enumerate(labels):
        box = (x + i * cell_w, y, x + (i + 1) * cell_w, y + cell_h)
        rounded_box(draw, box, fill, outline, width=2, radius=14)
        draw_center_text(draw, box, label, F_BODY_SMALL, fill="#1a202c")


def draw_architecture_chart():
    w, h = 1800, 980
    img = Image.new("RGB", (w, h), BG)
    draw = ImageDraw.Draw(img)

    label_w = 285
    margin_x = 28
    right = w - margin_x
    content_x = margin_x + label_w + 10
    row_gap = 48
    layers = [
        ((28, 18, right, 165), "用户层", "#dcebfa", "#9bb8cf"),
        ((28, 215, right, 455), "前端层（Vue3）", "#e4f7f7", "#8dc6c8"),
        ((28, 505, right, 750), "后端层\n（SpringBoot）", "#e6f6e9", "#93c9a1"),
        ((28, 805, right, 958), "数据层\n（MySQL）", "#ddecfb", "#8fb2d3"),
    ]

    for box, label, fill, outline in layers:
        draw.rounded_rectangle(box, radius=14, fill=fill, outline="#d6e4ef", width=2)
        draw_center_text(draw, (box[0] + 18, box[1], box[0] + label_w, box[3]), label, F_SUBTITLE, fill="#111827", line_gap=4)

    def person_icon(cx, cy, color=BLUE):
        draw.ellipse((cx - 12, cy - 26, cx + 12, cy - 2), fill=color)
        draw.pieslice((cx - 26, cy - 2, cx + 26, cy + 52), 180, 360, fill=color)
        draw.rectangle((cx - 26, cy + 24, cx + 26, cy + 28), fill=color)

    def admin_icon(cx, cy):
        person_icon(cx, cy, "#3678a8")

    def dorm_icon(cx, cy):
        draw.ellipse((cx - 12, cy - 28, cx + 12, cy - 4), fill="#3678a8")
        draw.polygon([(cx - 30, cy + 38), (cx + 30, cy + 38), (cx + 18, cy + 2), (cx - 18, cy + 2)], fill="#3678a8")
        draw.polygon([(cx - 10, cy + 3), (cx + 10, cy + 3), (cx + 5, cy + 33), (cx - 5, cy + 33)], fill="#eef7ff")
        draw.rectangle((cx - 8, cy + 14, cx + 8, cy + 18), fill="#3678a8")

    def role_box(x, y, text, icon_fn):
        box = (x, y, x + 450, y + 104)
        draw.rounded_rectangle(box, radius=12, fill="#f7fbff", outline="#8faec8", width=2)
        icon_fn(x + 88, y + 55)
        draw.text((x + 130, y + 32), text, font=F_BODY, fill="#111827")

    role_y = 40
    role_box(content_x + 0, role_y, "管理员（Admin）", admin_icon)
    role_box(content_x + 486, role_y, "宿管员（DormAdmin）", dorm_icon)
    role_box(content_x + 972, role_y, "学生（Student）", admin_icon)

    arrow(draw, (w // 2, 165), (w // 2, 215), fill="#111111", width=3, label="浏览器访问", label_pos=0.34, label_offset=(18, -5))

    panel_x, panel_r = content_x, right - 28
    front_panel = (panel_x, 228, panel_r, 372)
    draw.rounded_rectangle(front_panel, radius=12, fill="#d7f1f1", outline="#86bcc0", width=2)
    draw.regular_polygon((panel_x + 520, 263, 22), n_sides=6, rotation=math.pi / 6, outline="#5b9bd5", width=4)
    draw.text((panel_x + 555, 242), "Element Plus UI组件库", font=F_BODY, fill="#111827")

    module_labels = ["登录模块", "学生管理", "宿舍管理", "入住管理", "报修管理", "公告管理"]
    cell_w = 208
    gap = 22
    y = 294
    for i, label in enumerate(module_labels):
        x = panel_x + 30 + i * (cell_w + gap)
        draw.rounded_rectangle((x, y, x + cell_w, y + 66), radius=10, fill="#f9ffff", outline="#87bdc2", width=2)
        draw_center_text(draw, (x, y, x + cell_w, y + 66), label, F_BODY_SMALL, fill="#111827")

    strip = (panel_x, 386, panel_r, 442)
    draw.rounded_rectangle(strip, radius=8, fill="#98d0d5", outline="#98d0d5")
    draw_center_text(draw, strip, "Vue Router（路由管理）  + Axios（HTTP请求）  + Pinia（状态）", F_BODY, fill="#111827")

    arrow(draw, (w // 2, 455), (w // 2, 505), fill="#111111", width=3, label="RESTful API", label_pos=0.30, label_offset=(18, -5))

    back_panel = (panel_x, 520, panel_r, 666)
    draw.rounded_rectangle(back_panel, radius=12, fill="#ddf4e3", outline="#8ac59a", width=2)
    draw.ellipse((panel_x + 545, 535, panel_x + 587, 577), fill="#72b34a")
    draw.line((panel_x + 566, 543, panel_x + 566, 557), fill="#dff5d7", width=5)
    draw.arc((panel_x + 555, 546, panel_x + 577, 568), 30, 330, fill="#dff5d7", width=4)
    draw.text((panel_x + 602, 537), "Spring Boot 框架", font=F_BODY, fill="#111827")

    service_labels = ["用户认证\n(JWT)", "学生服务", "宿舍服务", "入住服务", "报修服务", "统计服务"]
    y = 578
    for i, label in enumerate(service_labels):
        x = panel_x + 28 + i * (cell_w + gap)
        draw.rounded_rectangle((x, y, x + cell_w, y + 72), radius=10, fill="#fbfffb", outline="#8ac59a", width=2)
        draw_center_text(draw, (x, y, x + cell_w, y + 72), label, F_BODY_SMALL, fill="#111827", line_gap=2)

    strip = (panel_x, 682, panel_r, 738)
    draw.rounded_rectangle(strip, radius=8, fill="#98d2aa", outline="#98d2aa")
    draw_center_text(draw, strip, "MyBatis-Plus（数据持久化）  + Maven（依赖管理）", F_BODY, fill="#111827")

    arrow(draw, (w // 2, 750), (w // 2, 805), fill="#111111", width=3, label="JDBC", label_pos=0.30, label_offset=(18, -5))

    data_groups = [
        ["用户表\n(User)", "学生表\n(Student)"],
        ["楼栋表\n(Building)", "宿舍表\n(Room)", "床位表\n(Bed)", "入住记录表\n(CheckIn)"],
        ["报修表\n(Repair)", "公告表\n(Notice)"],
    ]
    group_x = [content_x - 5, content_x + 360, content_x + 1080]
    group_w = [350, 700, 350]
    for gx, gw, labels in zip(group_x, group_w, data_groups):
        draw.rounded_rectangle((gx, 823, gx + gw, 945), radius=12, fill="#cfe4f8", outline="#7fa6c8", width=2)
        inner_gap = 12
        bw = (gw - inner_gap * (len(labels) + 1)) / len(labels)
        for i, label in enumerate(labels):
            x = gx + inner_gap + i * (bw + inner_gap)
            draw.rounded_rectangle((x, 838, x + bw, 930), radius=12, fill="#dcecfb", outline="#7fa6c8", width=2)
            draw_center_text(draw, (x, 838, x + bw, 930), label, F_BODY_SMALL, fill="#111827", line_gap=1)

    save_canvas(img, OUT_DIR / "图2-系统架构图.png")


if __name__ == "__main__":
    draw_flow_chart()
    draw_architecture_chart()
    print(OUT_DIR / "图1-开发与部署流程.png")
    print(OUT_DIR / "图2-系统架构图.png")
