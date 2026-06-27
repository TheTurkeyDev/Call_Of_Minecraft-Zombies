#!/usr/bin/env python3
"""
Build a PRUNED COM:Z custom resource pack from the BOZ source pack.

Strategy (Minecraft 1.21.4+ / 26.2 item-model system):
  * Add `assets/comz/items/<key>.json` item-definitions the plugin targets via
    setItemModel(comz:<key>); each points at an existing source model.
  * Copy ONLY the assets actually used:
      - models reachable from those item defs (following `parent`/`textures`/`overrides`),
      - the textures those models reference,
      - sounds, minus the big unused folders (maps / player voice-lines / easter-egg songs).
  * Write a modern pack.mcmeta (pack_format + wide supported_formats).

Keeps everything in the `minecraft` namespace (no texture-ref rewriting) and only adds `comz`
item defs. Result is a fraction of the 310 MB raw pack.

Usage: python3 build_pack.py "<source pack dir>" "<output dir>"
"""
import json, os, re, shutil, sys

PACK_FORMAT = 64
SUPPORTED_MIN = 34
SUPPORTED_MAX = 999
MC = "custom/item"
# Drop real 3D gun models here as resourcepack/models3d/<slug>/model.json (+ .png textures) to
# override a gun's fallback model. <slug>_pap/ overrides the Pack-a-Punch variant. See README.
MODELS3D = os.path.join(os.path.dirname(os.path.abspath(__file__)), "models3d")

# Sound folders to DROP. These are ~280 MB of full easter-egg music tracks, character voice
# lines and map-specific audio that NOTHING in the plugin triggers. Dropping them keeps every
# gameplay sound (weapons, perks, power-ups, rounds, mystery box, global) at a fraction of the
# size. Empty this tuple to ship literally every sound for future voice-line / EE wiring.
DROP_SOUND_PREFIXES = ("custom/egg", "custom/players", "custom/maps")

GUN = {
    "b23r":        (f"{MC}/3d_guns/cz75",          f"{MC}/3d_guns/cz75",            True),
    "executioner": (f"{MC}/1waw/sawn_off",         f"{MC}/1waw/sawn_off",           True),
    "five_seven":  (f"{MC}/3d_guns/m1911_v2",      f"{MC}/3d_guns/m1911_v2",        True),
    "kap40":       (f"{MC}/3d_guns/cz75",          f"{MC}/3d_guns/cz75",            True),
    "m1911":       (f"{MC}/3d_guns/m1911_v2",      f"{MC}/1waw/pap/c_3000",         False),
    "python":      (f"{MC}/3d_guns/python",        f"{MC}/2blops/pap/cobra",        False),
    "m1216":       (f"{MC}/2blops/spas_12",        f"{MC}/2blops/spas_12",          True),
    "olympia":     (f"{MC}/2blops/olympia",        f"{MC}/2blops/pap/hades",        False),
    "r870":        (f"{MC}/2blops/stakeout",       f"{MC}/2blops/stakeout",         True),
    "s12":         (f"{MC}/2blops/spas_12",        f"{MC}/2blops/spas_12",          True),
    "an94":        (f"{MC}/2blops/galil",          f"{MC}/2blops/galil",            True),
    "m16":         (f"{MC}/2blops/m16",            f"{MC}/2blops/pap/skullcrusher", False),
    "fal":         (f"{MC}/3d_guns/fal",           f"{MC}/2blops/pap/epc_wn",       False),
    "m8a1":        (f"{MC}/3d_guns/famas",         f"{MC}/3d_guns/famas",           False),
    "m14":         (f"{MC}/2blops/m14",            f"{MC}/2blops/pap/mnesia",       False),
    "m27":         (f"{MC}/2blops/commando",       f"{MC}/2blops/commando",         True),
    "mtar":        (f"{MC}/3d_guns/aug",           f"{MC}/3d_guns/aug",             False),
    "smr":         (f"{MC}/2blops/m14",            f"{MC}/2blops/m14",              True),
    "type25":      (f"{MC}/2blops/commando",       f"{MC}/2blops/commando",         True),
    "hamr":        (f"{MC}/2blops/hk21",           f"{MC}/2blops/hk21",             True),
    "lsat":        (f"{MC}/2blops/hk21",           f"{MC}/2blops/hk21",             True),
    "rpd":         (f"{MC}/2blops/rpk",            f"{MC}/2blops/rpk",              True),
    "mk48":        (f"{MC}/2blops/hk21",           f"{MC}/2blops/hk21",             True),
    "chicom":      (f"{MC}/2blops/mpl",            f"{MC}/2blops/mpl",              True),
    "mp5":         (f"{MC}/2blops/mp5",            f"{MC}/2blops/pap/mp115",        False),
    "pdw57":       (f"{MC}/3d_guns/spectre",       f"{MC}/3d_guns/spectre",         True),
    "msmc":        (f"{MC}/2blops/pm63",           f"{MC}/2blops/pm63",             True),
    "skorpion":    (f"{MC}/2blops/pm63",           f"{MC}/2blops/pm63",             True),
    "vector":      (f"{MC}/3d_guns/spectre",       f"{MC}/3d_guns/spectre",         True),
    "peacekeeper": (f"{MC}/2blops/ak74u",          f"{MC}/2blops/ak74u",            True),
    "barret":      (f"{MC}/2blops/l96",            f"{MC}/2blops/l96",              True),
    "dsr50":       (f"{MC}/2blops/l96",            f"{MC}/2blops/l96",              True),
    "svu":         (f"{MC}/2blops/dragunov",       f"{MC}/2blops/dragunov",         False),
    "raygun":      (f"{MC}/3d_guns/raygun_v2",     f"{MC}/wonder/pap/porter_x2",    False),
    "thundergun":  (f"{MC}/wonder/thundergun",     f"{MC}/wonder/thundergun",       False),
    "wunderwaffe": (f"{MC}/wonder/wunderwaffe",    f"{MC}/wonder/wunderwaffe",      False),
}
# Perks are no longer items — they render as reskinned vanilla potion-effect icons. The pack
# overrides each effect's mob_effect/<effect>.png from the local perk-icons/ folder (see below).
POWERUP = {
    "max_ammo": f"{MC}/powerups/max_ammo", "insta_kill": f"{MC}/powerups/insta_kill",
    "carpenter": f"{MC}/powerups/carpenter", "nuke": f"{MC}/powerups/nuke",
    "double_points": f"{MC}/powerups/double_points", "fire_sale": f"{MC}/powerups/fire_sale",
    "bonus_points": f"{MC}/powerups/powerup_base", "random_perk": f"{MC}/powerups/random_perk",
    "death_machine": f"{MC}/powerups/death_machine", "bonfire_sale": f"{MC}/powerups/bonfire_sale",
}
MISC = {"box_teddy": f"{MC}/teddies/box_teddy"}


def strip_ns(ref):
    return ref.split(":", 1)[1] if ":" in ref else ref


def resolve_models(src_models, roots):
    """Return the set of model paths (no .json) reachable from roots via parent/overrides,
    and the set of texture refs they use."""
    seen, textures, stack = set(), set(), list(roots)
    while stack:
        m = stack.pop()
        if m in seen:
            continue
        f = os.path.join(src_models, m + ".json")
        if not os.path.isfile(f):
            continue  # vanilla parent (item/generated, builtin/*, ...) — not in pack, leave as ref
        seen.add(m)
        try:
            data = json.load(open(f))
        except Exception:
            continue
        if isinstance(data.get("parent"), str):
            stack.append(strip_ns(data["parent"]))
        for v in (data.get("textures") or {}).values():
            if isinstance(v, str) and not v.startswith("#"):
                textures.add(strip_ns(v))
        for ov in (data.get("overrides") or []):
            if isinstance(ov.get("model"), str):
                stack.append(strip_ns(ov["model"]))
    return seen, textures


def copy_rel(src_root, dst_root, rel):
    s = os.path.join(src_root, rel)
    if not os.path.isfile(s):
        return False
    d = os.path.join(dst_root, rel)
    os.makedirs(os.path.dirname(d), exist_ok=True)
    shutil.copy2(s, d)
    return True


def main():
    src = sys.argv[1] if len(sys.argv) > 1 else \
        "/Users/ignace.mella/Downloads/ZombiesMC-1.20/Alpha V3.2.0.1 Release/Alpha V3.2.0.1 Resources"
    out = sys.argv[2] if len(sys.argv) > 2 else os.path.join(os.path.dirname(__file__), "build", "comz-pack")
    if not os.path.isdir(src):
        sys.exit(f"Source pack not found: {src}")
    if os.path.isdir(out):
        shutil.rmtree(out)

    src_mc = os.path.join(src, "assets", "minecraft")
    out_mc = os.path.join(out, "assets", "minecraft")
    src_models = os.path.join(src_mc, "models")
    src_tex = os.path.join(src_mc, "textures")
    os.makedirs(out, exist_ok=True)

    # pack.mcmeta + pack.png
    json.dump({"pack": {
        "pack_format": PACK_FORMAT,
        "supported_formats": {"min_inclusive": SUPPORTED_MIN, "max_inclusive": SUPPORTED_MAX},
        "description": "§cCall of Minecraft: §6Zombies §7custom pack",
    }}, open(os.path.join(out, "pack.mcmeta"), "w"), indent=2)
    if os.path.isfile(os.path.join(src, "pack.png")):
        shutil.copy2(os.path.join(src, "pack.png"), os.path.join(out, "pack.png"))

    # Atlas source: 1.21.4+/26.2 only stitches the default texture dirs (block/, item/, ...) into
    # the item atlas. This pack's textures live under textures/custom/**, so without registering
    # that directory the item models resolve geometry but show missing (black/purple) textures.
    # Atlas sources MERGE across packs, so adding just the custom dir is additive (vanilla intact).
    # source "custom" + prefix "custom/" => textures/custom/item/2blops/m14.png resolves to the id
    # "custom/item/2blops/m14", exactly what the models reference.
    atlas_dir = os.path.join(out_mc, "atlases")
    os.makedirs(atlas_dir, exist_ok=True)
    json.dump({"sources": [{"type": "directory", "source": "custom", "prefix": "custom/"}]},
              open(os.path.join(atlas_dir, "blocks.json"), "w"), indent=2)

    # 1) comz item definitions + collect root model paths
    items_root = os.path.join(out, "assets", "comz", "items")
    roots, written, missing = set(), 0, []

    def write_def(key, model_ref):
        nonlocal written
        p = os.path.join(items_root, *key.split("/")) + ".json"
        os.makedirs(os.path.dirname(p), exist_ok=True)
        json.dump({"model": {"type": "minecraft:model", "model": model_ref}}, open(p, "w"), indent=2)
        written += 1

    def emit(key, model_path):
        if not os.path.isfile(os.path.join(src_models, model_path + ".json")):
            missing.append((key, model_path))
        else:
            roots.add(model_path)
        write_def(key, "minecraft:" + model_path)

    def import_3d(name):
        """Import a user-supplied 3D model from resourcepack/models3d/<name>/ into the comz
        namespace. The dir holds one model JSON (model.json or *.json) plus its .png textures;
        texture refs are rewritten to comz:gun/<name>/<tex> and the model lands at
        assets/comz/models/gun/<name>.json. Returns True if imported, False if no override dir."""
        d = os.path.join(MODELS3D, name)
        if not os.path.isdir(d):
            return False
        mj = os.path.join(d, "model.json")
        if not os.path.isfile(mj):
            cands = [f for f in os.listdir(d) if f.endswith(".json")]
            if not cands:
                return False
            mj = os.path.join(d, cands[0])
        model = json.load(open(mj))
        # Textures land under textures/custom/gun3d/<name>/ so the existing custom/ atlas source
        # stitches them (no extra atlas needed); models reference them with the bare custom/ id.
        tex_out = os.path.join(out_mc, "textures", "custom", "gun3d", name)
        for k, v in list((model.get("textures") or {}).items()):
            if not isinstance(v, str) or v.startswith("#"):
                continue
            base = os.path.basename(strip_ns(v))
            src_png = os.path.join(d, base + ".png")
            if not os.path.isfile(src_png):  # tolerate value already being a bare filename
                src_png = os.path.join(d, base)
            if os.path.isfile(src_png):
                os.makedirs(tex_out, exist_ok=True)
                shutil.copy2(src_png, os.path.join(tex_out, base + ".png"))
            model["textures"][k] = f"custom/gun3d/{name}/{base}"
        mdl_out = os.path.join(out, "assets", "comz", "models", "gun", name + ".json")
        os.makedirs(os.path.dirname(mdl_out), exist_ok=True)
        json.dump(model, open(mdl_out, "w"), indent=2)
        return True

    imported3d = []

    def emit_gun(key, name, fallback_model):
        if import_3d(name):
            write_def(key, f"comz:gun/{name}")
            imported3d.append(name)
        else:
            emit(key, fallback_model)

    for slug, (base, pap, _dl) in GUN.items():
        emit_gun(f"gun/{slug}", slug, base)
        emit_gun(f"gun/{slug}_pap", slug + "_pap", pap)
    for slug, mp in POWERUP.items():
        emit(f"powerup/{slug}", mp)
    for slug, mp in MISC.items():
        emit(f"misc/{slug}", mp)

    # Throwables: monkey bomb has a model; grenade has only a texture (custom/item/lethal/m67),
    # so we generate a simple comz item model for it. Both replace the vanilla slime/magma look.
    emit("throwable/monkey_bomb", f"{MC}/wonder/monkey_bomb")
    grenade_model_dir = os.path.join(out, "assets", "comz", "models", "item")
    os.makedirs(grenade_model_dir, exist_ok=True)
    json.dump({"parent": "item/generated", "textures": {"layer0": "minecraft:custom/item/lethal/m67"}},
              open(os.path.join(grenade_model_dir, "grenade.json"), "w"), indent=2)
    p = os.path.join(items_root, "throwable", "grenade.json")
    os.makedirs(os.path.dirname(p), exist_ok=True)
    json.dump({"model": {"type": "minecraft:model", "model": "comz:item/grenade"}}, open(p, "w"), indent=2)
    written += 1

    # 2) model closure + textures, copy only those
    models, textures = resolve_models(src_models, roots)
    textures.add("custom/item/lethal/m67")  # grenade model texture (not reachable via a source model)
    for m in models:
        copy_rel(src_models, os.path.join(out_mc, "models"), m + ".json")
    tex_copied = 0
    for t in textures:
        if copy_rel(src_tex, os.path.join(out_mc, "textures"), t + ".png"):
            tex_copied += 1
            copy_rel(src_tex, os.path.join(out_mc, "textures"), t + ".png.mcmeta")  # animation meta if any

    # 2b) perk HUD icons: override vanilla mob_effect/<effect>.png from the local perk-icons/ folder.
    # Perks render as reskinned potion-effect icons (PerkType.java maps each perk to one effect).
    perk_icons_dir = os.path.join(os.path.dirname(os.path.abspath(__file__)), "perk-icons")
    mob_effect_out = os.path.join(out_mc, "textures", "mob_effect")
    perk_icons_copied = 0
    if os.path.isdir(perk_icons_dir):
        os.makedirs(mob_effect_out, exist_ok=True)
        for fn in sorted(os.listdir(perk_icons_dir)):
            if fn.endswith(".png"):
                shutil.copy2(os.path.join(perk_icons_dir, fn), os.path.join(mob_effect_out, fn))
                perk_icons_copied += 1

    # 3) sounds: filter sounds.json + copy only kept oggs
    sounds_json = os.path.join(src_mc, "sounds.json")
    kept_oggs, oggs_copied = set(), 0
    if os.path.isfile(sounds_json):
        data = json.load(open(sounds_json))

        def keep_path(p):
            p = strip_ns(p)
            return not any(p.startswith(pre) for pre in DROP_SOUND_PREFIXES)

        filtered = {}
        for event, body in data.items():
            if not isinstance(body, dict) or "sounds" not in body:
                filtered[event] = body
                continue
            new_sounds = []
            for s in body["sounds"]:
                path = s if isinstance(s, str) else s.get("name", "")
                if keep_path(path):
                    new_sounds.append(s)
                    kept_oggs.add(strip_ns(path))
            if new_sounds:
                nb = dict(body); nb["sounds"] = new_sounds
                filtered[event] = nb
        os.makedirs(out_mc, exist_ok=True)
        json.dump(filtered, open(os.path.join(out_mc, "sounds.json"), "w"), indent=2)
        for o in kept_oggs:
            if copy_rel(os.path.join(src_mc, "sounds"), os.path.join(out_mc, "sounds"), o + ".ogg"):
                oggs_copied += 1

    # report
    print(f"item defs:   {written}" + (f"  ({len(missing)} MISSING!)" if missing else "  (all models exist ✓)"))
    for k, m in missing:
        print(f"   MISSING {k} -> {m}")
    print(f"models:      {len(models)} copied")
    print(f"textures:    {tex_copied} copied")
    print(f"perk icons:  {perk_icons_copied} mob_effect overrides copied")
    print(f"sounds:      {oggs_copied} oggs copied (dropped folders: {', '.join(DROP_SOUND_PREFIXES)})")
    if imported3d:
        print(f"3D overrides imported from models3d/: {len(imported3d)} -> {', '.join(sorted(imported3d))}")
    needs = [s for s, (_b, _p, dl) in GUN.items() if dl and s not in imported3d]
    print(f"fallback guns (drop a model in models3d/<slug>/ to replace): {len(needs)} -> {', '.join(needs)}")


if __name__ == "__main__":
    main()
