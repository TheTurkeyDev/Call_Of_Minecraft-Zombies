# COM:Z Custom Resource Pack

The complete custom pack for Call of Minecraft: Zombies lives here as plain project files
under **`comz-pack/`** — custom gun / perk / power-up / throwable models, perk-effect icons, and
all gameplay sounds, ready for Minecraft 1.21.4+ / 26.2. No source packs or build step needed;
edit the files directly.

## Layout (`comz-pack/`)

- `pack.mcmeta` — modern `pack_format` + wide `supported_formats`.
- `assets/comz/items/**` — item-definition files the plugin targets with
  `setItemModel(comz:<key>)`: `gun/<slug>(_pap)`, `perk/<slug>`, `powerup/<slug>`,
  `throwable/{grenade,monkey_bomb}`, `misc/{box_teddy,revive}`.
- `assets/comz/models/item/grenade.json` — the one generated model (grenade, from the m67 texture).
- `assets/minecraft/models|textures/custom/**` — the gun/power-up/teddy models + textures.
- `assets/minecraft/textures/mob_effect/<effect>.png` — perk icons. Perks are not items; each
  perk rides on a unique vanilla `PotionEffectType` (see `PerkType.java`) and the pack overrides
  that effect's `mob_effect` icon with the perk's bottle art. Source art lives in `perk-icons/`
  (named by effect) and is committed straight into `mob_effect/` here — no build step.
- `assets/minecraft/atlases/blocks.json` — registers `textures/custom/**` into the item atlas
  (1.21.4+ only stitches the default dirs otherwise → black/purple items).
- `assets/minecraft/sounds.json` + `sounds/custom/**` — all gameplay sounds (weapons, perks,
  power-ups, rounds, mystery box, global). See `gun-model-mapping.md` for which model each gun uses.

## How the plugin uses it

- **Models** — the plugin sets the `minecraft:item_model` component on items via
  `ItemMeta.setItemModel(comz:<key>)` (see `util/PackModels.java`). Guns read their key from
  `guns.json` (`item_model`); power-ups carry keys in `PowerUp`. Without the pack installed the
  component is ignored and items keep their vanilla look.
- **Perk icons** — each perk maps to one `PotionEffectType` in `PerkType.java`; the pack overrides
  that effect's `mob_effect/<effect>.png`. Without the pack the player sees the plain vanilla
  effect icon.
- **Sounds** — `util/SoundUtil.java` plays a string that is either a Bukkit enum name OR a
  namespaced pack event. **All** sound mappings live in the dedicated **`sounds.json`** config
  (`plugins/COM_Zombies/sounds.json`): event sounds (`perk`, `box`, `round`, `packapunch`,
  `teleporter`, `door`, `trap`, `gun.buy`, `game.join`), per-power-up (`powerup.<TYPE>`), and
  per-gun overrides (`guns.<GunName>.shoot|reload`, with `_default`/`_defaultPaP` fallbacks that
  themselves fall back to guns.json `sound`/`reload_sound`). Defaults are vanilla enums; point any
  value at a `comz:` event to use the pack's audio. The pack also `replace:true`s several vanilla
  events (glass break, explosions, …) so those are remapped automatically.

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
- **Perk icons come from BO2 `.iwi` assets** — `perk-icons/*.png` are real BO2 perk icons
  converted via `perk-icons/iwi_to_png.py` (DXT5 → DDS → PNG); `generate_placeholders.py` is a
  fallback generator. Replace any `<effect>.png`, copy it into
  `comz-pack/assets/minecraft/textures/mob_effect/`, done — no code change needed.
- **Boss models** (George/Brutus) cannot be reskinned individually by a resource pack — a reskin
  would change all zombies/piglin-brutes. Only their sounds/name/glow distinguish them.
