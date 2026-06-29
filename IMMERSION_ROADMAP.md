# Call of Minecraft: Zombies — Immersion Roadmap

BO2-style "juice": HUD, audio stingers, particles, and Display-entity VFX layered on top of the
existing gameplay. This file tracks what shipped, the shared infra it runs on, and what's designed
but not yet built.

> **Every visual is toggleable** under `config.visuals.*` and **defaults to `true`**. Set any flag
> to `false` to silence that one effect; nothing here changes gameplay numbers. See
> [Config](#config) for the full key list.

---

## Shipped this pass

| Feature | What you see/hear | Config flag |
|---|---|---|
| **Ammo action-bar HUD** | Live `mag / reserve` ammo (and reload state) on the action bar while holding a gun. | `config.visuals.ammoHud` |
| **Round-start stinger + darkness** | Round-change sound sting plus a brief screen-darkening / fade as the new wave begins. | `config.visuals.roundStinger` |
| **Zombie blood + headshot numbers** | Red blood particle burst on hits; floating damage number that pops bigger/gold on a headshot. | `config.visuals.hitNumbers` |
| **Boss health BossBar + stinger** | A boss-colored BossBar tracks George/Brutus HP for the arena; a stinger plays on boss spawn. | `config.visuals.bossBar` |
| **Mystery-box light beam** | A vertical light beam / particle column marks the active Mystery Box location. | `config.visuals.boxBeam` |
| **Perk buy jingle** | The signature per-perk jingle plays when a perk is purchased. | `config.visuals.perkJingle` |
| **Pack-a-Punch UPGRADED feedback** | "UPGRADED" title/particle burst + sound when a gun finishes Pack-a-Punch. | `config.visuals.papFeedback` |
| **Downed player polish** *(earlier)* | Sit-pose on the downed body, a revive icon above them, and a revive progress bar for the reviver. | `config.visuals.downedFx` |
| **Power-up glow + beam** *(earlier)* | Dropped power-ups glow and emit a colored beam so they read at a glance. | `config.visuals.powerUpFx` |
| **Nuke VFX** *(earlier)* | Screen flash / particle sweep when a Nuke power-up clears the board. | `config.visuals.nukeFx` |

---

## Shared infra

Two reusable helpers were added under `util/` — **use these for any future visual** rather than
hand-rolling particle/entity code:

- **`util/ParticleFX`** — wrappers for the recurring particle patterns (bursts, beams/columns,
  sweeps, blood). Centralizes counts/offsets/colors so effects stay consistent and cheap.
- **`util/DisplayEntityUtil`** — spawn/position/cleanup helpers for **Display entities**
  (`TextDisplay` for floating numbers & labels, `ItemDisplay`/`BlockDisplay` for 3D props). Handles
  the lifecycle (spawn → billboard/transform → auto-remove) so callers don't leak entities.

Floating damage numbers, the box beam, and the revive icon already route through these; new VFX
should too.

---

## Deferred (designed, not yet built)

Each item below is scoped but not implemented. Sketch = the file/API it would hang off.

- **Full Mystery-box teddy-bear spin + spinning-gun reveal**
  Replace the instant box result with an animated reveal: an `ItemDisplay` (via `DisplayEntityUtil`)
  cycling gun models over the box with a Transformation spin, settling on the result; teddy-bear
  jump-out uses a second `ItemDisplay` + the existing box-move audio. Drive from the box-roll code
  in the Mystery Box manager.

- **Per-map fog / atmosphere** (`mapTheme` in `arenas.json`)
  Add an optional `mapTheme` block per arena (fog color/density, ambient sound bed, sky tint). Apply
  on game start by sending per-player fog/biome-style packets (or `WorldBorder` warning-distance fog
  as a no-NMS fallback) and looping the ambient bed via `SoundUtil`. Read in the arena loader; gate
  behind `config.visuals.mapTheme`.

- **Gun ADS / recoil / muzzle polish**
  Camera kick on fire, a muzzle-flash particle at the barrel, and an ADS zoom/FOV cue. Hook the
  shoot path (gun fire handler); muzzle flash via `ParticleFX`, recoil via small pitch nudges,
  ADS via the existing zoom plumbing (`ZoomTexture`).

- **Between-round shop / pause-menu GUI**
  An inventory-`Menu` (Bukkit `InventoryHolder`/chest GUI) opened in the round-transition gap to
  buy perks/ammo/Gobblegums. New `gui/` menu class wired to the round-end event; respects the
  player's points balance.

- **Gobblegum-style modifiers**
  Consumable one-shot/round buffs (e.g. instant-revive, double points window). A `Gobblegum` enum +
  per-player active-effect tracker checked at the relevant hooks; granted via the shop GUI above or
  a power-up. Persist owned gums in `playerdata.json`.

- **Perk-machine 3D models**
  Swap perk-machine signs/blocks for real 3D machine models using `BlockDisplay`/`ItemDisplay`
  (`DisplayEntityUtil`) with the modern `minecraft:item_model` component (`ItemMeta#setItemModel`,
  same path as `util/PackModels`). Authored as pack models under `assets/comz/items/perk/…`; falls
  back to the sign when no pack is installed.

---

## Config

All immersion effects live under **`config.visuals.*`** in `plugins/COM_Zombies/config.yml`. **Every
flag defaults to `true`** — the full experience is on out of the box. Flip an individual flag to
`false` to disable just that effect (no gameplay impact). Deferred features that gain a flag are
noted inline above (`config.visuals.mapTheme`, etc.).

| Flag | Effect |
|---|---|
| `ammoHud` | Ammo action-bar HUD |
| `roundStinger` | Round-start stinger + darkness |
| `hitNumbers` | Blood particles + headshot damage numbers |
| `bossBar` | Boss health BossBar + spawn stinger |
| `boxBeam` | Mystery-box light beam |
| `perkJingle` | Perk-purchase jingle |
| `papFeedback` | Pack-a-Punch UPGRADED feedback |
| `downedFx` | Downed sit-pose, revive icon, progress bar |
| `powerUpFx` | Power-up glow + beam |
| `nukeFx` | Nuke screen flash / sweep |
