# Importing real 3D gun models

Drop true 3D models here to replace a gun's fallback model. `build_pack.py` picks them up
automatically — no code change needed.

## Layout

```
resourcepack/models3d/
  <slug>/            # base gun  (e.g. an94/)
    model.json       # Blockbench export (or any single *.json)
    <texture>.png    # every texture the model references, by basename
  <slug>_pap/        # optional: Pack-a-Punch variant (e.g. an94_pap/)
    model.json
    <texture>.png
```

`<slug>` is the gun key from `build_pack.py` / `gun-model-mapping.md`
(`b23r, executioner, five_seven, kap40, m1216, r870, s12, an94, m8a1, m27, mtar, smr,
type25, hamr, lsat, rpd, mk48, chicom, pdw57, msmc, skorpion, vector, peacekeeper,
barret, dsr50, ...`). The 25 "fallback" guns are the ones worth replacing first.

## How it works

For each `<slug>/`, the builder:
1. copies the model JSON to `assets/comz/models/gun/<slug>.json`,
2. copies its `.png` textures to `assets/minecraft/textures/custom/gun3d/<slug>/` and rewrites
   the model's `textures` entries to `custom/gun3d/<slug>/<name>` (so the existing `custom/`
   atlas source stitches them — no extra setup),
3. points the item definition `comz:gun/<slug>` at the imported model.

If no `<slug>/` dir exists, the gun keeps its source-pack fallback model. So you can convert
guns to true 3D one at a time and just re-run `build_pack.py`.

## Notes

- `model.json` must be self-contained (a Blockbench "Java Block/Item" export is). Texture
  values should be plain names matching the `.png` files in the same folder.
- Use an `item/handheld`-style display block (or Blockbench's "third/first person" transforms)
  so the gun sits correctly in hand.
- Keep filenames lowercase. After dropping files, rerun:
  `python3 resourcepack/build_pack.py "<source pack>" resourcepack/build/comz-pack`
