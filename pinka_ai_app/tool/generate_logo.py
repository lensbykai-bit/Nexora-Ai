from PIL import Image, ImageDraw, ImageFont, ImageFilter
from pathlib import Path

SIZE = 1024
out = Path('assets/images')
out.mkdir(parents=True, exist_ok=True)

img = Image.new('RGBA', (SIZE, SIZE), (22, 0, 15, 255))

# Pink radial-style glow layers
for radius, alpha in [(470, 35), (390, 55), (310, 75), (230, 95)]:
    layer = Image.new('RGBA', (SIZE, SIZE), (0, 0, 0, 0))
    d = ImageDraw.Draw(layer)
    box = (SIZE//2-radius, SIZE//2-radius, SIZE//2+radius, SIZE//2+radius)
    d.ellipse(box, fill=(255, 35, 145, alpha))
    layer = layer.filter(ImageFilter.GaussianBlur(70))
    img = Image.alpha_composite(img, layer)

# Rounded app tile
draw = ImageDraw.Draw(img)
draw.rounded_rectangle((70, 70, 954, 954), radius=210, fill=(54, 4, 37, 245), outline=(255, 105, 186, 255), width=18)
draw.rounded_rectangle((92, 92, 932, 932), radius=190, outline=(255, 205, 232, 180), width=6)

# Inner ring and glow
ring = Image.new('RGBA', (SIZE, SIZE), (0, 0, 0, 0))
rd = ImageDraw.Draw(ring)
rd.ellipse((220, 210, 804, 794), fill=(40, 2, 28, 220), outline=(255, 58, 158, 255), width=20)
rd.ellipse((245, 235, 779, 769), outline=(255, 180, 218, 180), width=7)
glow = ring.filter(ImageFilter.GaussianBlur(34))
img = Image.alpha_composite(img, glow)
img = Image.alpha_composite(img, ring)
draw = ImageDraw.Draw(img)

# Large P
font_candidates = [
    '/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf',
    '/usr/share/fonts/truetype/liberation2/LiberationSans-Bold.ttf',
]
font = None
for p in font_candidates:
    try:
        font = ImageFont.truetype(p, 520)
        break
    except OSError:
        pass
if font is None:
    font = ImageFont.load_default()

text = 'P'
bbox = draw.textbbox((0, 0), text, font=font, stroke_width=0)
tw = bbox[2] - bbox[0]
th = bbox[3] - bbox[1]
x = (SIZE - tw) // 2 - 25
y = (SIZE - th) // 2 - 95

# shadow/extrusion
draw.text((x+18, y+25), text, font=font, fill=(120, 0, 65, 255), stroke_width=14, stroke_fill=(75, 0, 45, 255))
draw.text((x, y), text, font=font, fill=(255, 205, 232, 255), stroke_width=12, stroke_fill=(255, 60, 155, 255))
draw.text((x+5, y+3), text, font=font, fill=(255, 235, 246, 255), stroke_width=3, stroke_fill=(255, 170, 215, 255))

# Play triangle inside P area
triangle = [(500, 435), (500, 565), (610, 500)]
draw.polygon(triangle, fill=(255, 72, 166, 255), outline=(255, 235, 246, 255))

# Audio waveform
base_x = 635
base_y = 650
heights = [60, 95, 140, 90, 180, 125, 210, 115, 160]
for i, h in enumerate(heights):
    xx = base_x + i * 24
    draw.rounded_rectangle((xx, base_y-h//2, xx+11, base_y+h//2), radius=6, fill=(255, 135, 199, 255))

# sparkles
for sx, sy, r in [(215, 230, 18), (820, 250, 14), (825, 790, 11), (190, 760, 10)]:
    draw.line((sx-r, sy, sx+r, sy), fill=(255, 230, 244, 255), width=5)
    draw.line((sx, sy-r, sx, sy+r), fill=(255, 230, 244, 255), width=5)

img.convert('RGB').save(out / 'pinka_logo.png', quality=95)
print(out / 'pinka_logo.png')
