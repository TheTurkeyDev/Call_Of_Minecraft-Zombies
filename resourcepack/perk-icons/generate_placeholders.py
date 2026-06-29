#!/usr/bin/env python3
"""
Generate PLACEHOLDER perk HUD icons until real BO2 perk-bottle art is dropped in.

Perks now render as reskinned vanilla potion-effect icons (see PerkType.java). Each perk maps to
one vanilla effect; the resource pack overrides that effect's `mob_effect/<effect>.png`. This script
writes one PNG per effect here in `perk-icons/`, named by the effect. `build_pack.py` copies them
into `assets/minecraft/textures/mob_effect/` at build time.

To ship real art: replace the matching `<effect>.png` files in this folder (any square size; MC
scales) and rerun `build_pack.py`. No code change needed.
"""
import os
from PIL import Image, ImageDraw, ImageFont

SIZE = 64  # vanilla icons are 18x18; packs may supply higher-res, MC downscales.

# perk -> (vanilla effect filename, label, bottle RGB) — must match PerkType.java mapping.
PERKS = [
    ("Juggernog",        "hero_of_the_village", "JU",  (200, 48, 46)),
    ("Speed Cola",       "luck",                "SC",  (63, 165, 53)),
    ("Quick Revive",     "unluck",              "QR",  (90, 160, 216)),
    ("Double Tap",       "dolphins_grace",      "DT",  (224, 176, 32)),
    ("Stamin-Up",        "trial_omen",          "SU",  (168, 200, 60)),
    ("PhD Flopper",      "fire_resistance",     "PH",  (224, 122, 30)),
    ("Deadshot Daiquiri","saturation",          "DS",  (46, 95, 160)),
    ("Mule Kick",        "bad_omen",            "MK",  (156, 107, 60)),
    ("Electric Cherry",  "conduit_power",       "EC",  (208, 48, 72)),
    ("Vulture Aid",      "water_breathing",     "VA",  (47, 160, 160)),
    ("Tombstone Soda",   "wind_charged",        "TS",  (122, 63, 165)),
    ("Who's Who",        "raid_omen",           "WW",  (70, 200, 210)),
]


def load_font(px):
    for path in (
        "/System/Library/Fonts/Supplemental/Arial Bold.ttf",
        "/System/Library/Fonts/HelveticaNeue.ttc",
        "/Library/Fonts/Arial Bold.ttf",
    ):
        if os.path.isfile(path):
            try:
                return ImageFont.truetype(path, px)
            except Exception:
                pass
    return ImageFont.load_default()


def darker(c, f=0.55):
    return tuple(int(x * f) for x in c)


def make_icon(label, color):
    img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    s = SIZE
    outline = darker(color)
    # simple soda-bottle silhouette: neck + rounded body
    neck_w = s * 0.22
    d.rectangle([s/2 - neck_w/2, s*0.08, s/2 + neck_w/2, s*0.26], fill=outline)
    d.rectangle([s/2 - neck_w/2 + 2, s*0.10, s/2 + neck_w/2 - 2, s*0.26], fill=color)
    d.rounded_rectangle([s*0.18, s*0.24, s*0.82, s*0.95], radius=int(s*0.16),
                        fill=color, outline=outline, width=max(2, s//24))
    # label
    font = load_font(int(s * 0.34))
    tb = d.textbbox((0, 0), label, font=font)
    tw, th = tb[2] - tb[0], tb[3] - tb[1]
    d.text((s/2 - tw/2 - tb[0], s*0.55 - th/2 - tb[1]), label, font=font,
           fill=(255, 255, 255, 255), stroke_width=max(1, s//40), stroke_fill=outline)
    return img


def main():
    here = os.path.dirname(os.path.abspath(__file__))
    for name, effect, label, color in PERKS:
        make_icon(label, color).save(os.path.join(here, effect + ".png"))
        print(f"{name:18s} -> mob_effect/{effect}.png")


if __name__ == "__main__":
    main()
