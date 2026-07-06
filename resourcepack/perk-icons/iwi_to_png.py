#!/usr/bin/env python3
"""
Convert Black Ops 2 .iwi perk textures into the perk HUD icon PNGs this pack ships.

BO2 .iwi (version 0x1B) is an Infinity Ward image: small header then DXT-compressed pixel data
(mips stored smallest-first, so the full-res base mip is the trailing block). We read the format +
dimensions from the header, slice off the base mip, wrap it in a minimal DDS container, and let
Pillow (which decodes DXT1/3/5) turn it into RGBA -> PNG named by the mapped vanilla effect.

Source assets: the user's "Black Ops 2 HD Perks Shaders/Perks" folder. Output overwrites the
placeholder <effect>.png files here; build_pack.py copies them into mob_effect/.
"""
import io, os, struct, sys
from PIL import Image

# IWI format byte -> (fourcc or None for uncompressed, block bytes per 4x4, bpp-uncompressed)
IWI_FMT = {0x0B: ("DXT1", 8), 0x0C: ("DXT3", 16), 0x0D: ("DXT5", 16)}

# iwi basename (no ext) -> mapped vanilla effect filename (must match PerkType.java)
MAP = {
    "specialty_juggernaut_zombies": "hero_of_the_village",  # Juggernog
    "specialty_fastreload_zombies": "luck",                 # Speed Cola
    "specialty_quickrevive_zombies": "unluck",              # Quick Revive
    "specialty_doubletap_zombies": "dolphins_grace",        # Double Tap
    "specialty_marathon_zombies": "trial_omen",             # Stamin-Up (move boost via walk-speed)
    "specialty_divetonuke_zombies": "fire_resistance",      # PhD Flopper
    "specialty_ads_zombies": "saturation",                  # Deadshot Daiquiri (ADS)
    "specialty_mulekick_zombies": "bad_omen",               # Mule Kick
    "minimap_icon_electric_cherry": "conduit_power",        # Electric Cherry
    "specialty_vulture_zombies": "water_breathing",         # Vulture Aid
    "specialty_tombstone_zombies": "wind_charged",          # Tombstone Soda
    "minimap_icon_chugabud": "raid_omen",                   # Who's Who (Chugabud)
}


def dds_header(w, h, fourcc):
    # 124-byte DDS header for a DXT-compressed 2D texture, no mips.
    DDSD = 0x1 | 0x2 | 0x4 | 0x1000 | 0x80000  # caps|height|width|pixelformat|linearsize
    linsize = max(1, w // 4) * max(1, h // 4) * (8 if fourcc == b"DXT1" else 16)
    hdr = b"DDS " + struct.pack("<I", 124)
    hdr += struct.pack("<IIIII", DDSD, h, w, linsize, 0)
    hdr += struct.pack("<I", 0) + b"\x00" * 44          # depth + reserved[11]
    hdr += struct.pack("<II", 32, 0x4) + fourcc          # pixelformat: size, flags=FOURCC, fourcc
    hdr += struct.pack("<IIIII", 0, 0, 0, 0, 0)          # rgb bit counts / masks
    hdr += struct.pack("<IIIII", 0x1000, 0, 0, 0, 0)     # caps = TEXTURE
    return hdr


def convert(path):
    data = open(path, "rb").read()
    if data[:3] != b"IWi":
        raise ValueError(f"not an IWI: {path}")
    fmt = data[4]
    width, height = struct.unpack_from("<HH", data, 6)
    if fmt not in IWI_FMT:
        raise ValueError(f"unsupported IWI format 0x{fmt:02x} in {path}")
    fourcc, block = IWI_FMT[fmt]
    base_size = max(1, width // 4) * max(1, height // 4) * block
    payload = data[-base_size:]  # base (largest) mip is the trailing block
    dds = dds_header(width, height, fourcc.encode()) + payload
    img = Image.open(io.BytesIO(dds)).convert("RGBA")
    return img


def main():
    src = sys.argv[1] if len(sys.argv) > 1 else \
        "/Users/ignace.mella/Downloads/Black Ops 2 HD Perks Shaders/Perks"
    here = os.path.dirname(os.path.abspath(__file__))
    for base, effect in MAP.items():
        p = os.path.join(src, base + ".iwi")
        if not os.path.isfile(p):
            print(f"  MISSING {base}.iwi")
            continue
        img = convert(p)
        out = os.path.join(here, effect + ".png")
        img.save(out)
        print(f"{base:34s} {img.size} -> {effect}.png")


if __name__ == "__main__":
    main()
