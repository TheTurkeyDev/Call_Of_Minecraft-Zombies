# COM:Z Custom Resource Pack

The complete custom pack for Call of Minecraft: Zombies lives here as plain project files
under **`comz-pack/`** — custom gun / perk / power-up / throwable models + all gameplay sounds,
ready for Minecraft 1.21.4+ / 26.2. No source packs or build step needed; edit the files
directly.

## Layout (`comz-pack/`)

- `pack.mcmeta` — modern `pack_format` + wide `supported_formats`.
- `assets/comz/items/**` — item-definition files the plugin targets with
  `setItemModel(comz:<key>)`: `gun/<slug>(_pap)`, `perk/<slug>`, `powerup/<slug>`,
  `throwable/{grenade,monkey_bomb}`, `misc/box_teddy`.
- `assets/comz/models/item/grenade.json` — the one generated model (grenade, from the m67 texture).
- `assets/minecraft/models|textures/custom/**` — the gun/power-up/teddy models + textures.
- `assets/minecraft/atlases/blocks.json` — registers `textures/custom/**` into the item atlas
  (1.21.4+ only stitches the default dirs otherwise → black/purple items).
- `assets/minecraft/sounds.json` + `sounds/custom/**` — all gameplay sounds (weapons, perks,
  power-ups, rounds, mystery box, global). See `gun-model-mapping.md` for which model each gun uses.

## Use it

1. Zip the **contents** of `comz-pack/` (so `pack.mcmeta` is at the zip root).
2. Host the zip (GitHub release asset, web server, …); `sha1sum` it.
3. In `config.yml`:
   ```yaml
   config:
     resourcePack:
       enabled: true
       url: "https://.../comz-pack.zip"
       sha1: "<sha1 of your zip>"
       force: true
   ```
4. Join an arena → the client downloads it; watch console for the pack status. If the client
   warns about the version, bump `pack_format` in `pack.mcmeta`.

## Notes / known gaps

- **23 guns use 2D-sprite models** — the source only had 9 true-3D gun models (all in use; 13 guns
  are 3D). To make one true-3D, replace its model under `assets/minecraft/models/custom/item/...`
  (+ texture) with a real 3D model and repoint its `assets/comz/items/gun/<slug>.json`.
- **Perks show vanilla icons** — no per-perk bottle models exist (gated off in `PerkType`).
- **Boss models** can't be reskinned by a vanilla pack (per-entity-type limitation).
- Sound mappings (which event each gameplay sound uses) live in the plugin's `sounds.json`
  config, not here.
