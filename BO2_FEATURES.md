# Call of Minecraft: Zombies — BO2 Fidelity Features

This document covers every feature added across the BO2-fidelity tiers (0–4) and how to use
them as a server owner or player. **Every value below is configurable** in
`plugins/COM_Zombies/config.yml` — defaults are shown, and any feature you don't want can be
turned off (toggle, or set a count/chance to `0`).

> **Build:** JDK 21, target 17, spigot-api 26.2. Drop the jar in `plugins/`, restart the server
> (not `/reload` — the plugin loads `POSTWORLD`). New config keys auto-append with defaults; to
> regenerate the fully-commented file, delete the old `config.yml` first.

---

## 1. Core mechanics (Tier 0)

These match the numbers/feel of Black Ops 2 Zombies. Mostly automatic; tune in `config.gameSettings`.

| Mechanic | Default | Key | Notes |
|---|---|---|---|
| Zombie health curve | — | (code) | Linear +100/round to round 9, then ×1.1 compounding. Late rounds get hard. |
| Board cap (alive at once) | 24 + 6/extra player | `zombieBoardBase`, `zombieBoardPerPlayer` | Scales with player count. |
| Zombies per round | round×0.15×board (r5+); r1–4 ramp .2/.4/.6/.8 | `zombieRoundMultiplier` | |
| Zombie melee damage | 10 (2 hits to down; ~5 with Jugg) | `zombieDamage` | |
| Per-zombie attack cooldown | 20 ticks (~1s) | `zombieAttackCooldownTicks` | Hits from different zombies **stack** (no shared invuln) — surrounded = fast down. |
| Health regen delay | 2.5s | `healTime` | |
| Headshot kill bonus | +30 pts | `headshotKillBonus` | On top of the normal kill reward. |
| Melee/knife kill bonus | +60 pts | `meleeKillBonus` | |
| Knife one-shot band | through round 9 | `knifeOneShotThroughRound` | Flat knife damage = the HP of that round. |
| Dogs per dog-round | 3 × players | `dogsPerPlayer` | Scales with players, not round. Guaranteed Max Ammo on the last dog (`dogRoundMaxAmmoDrop`). |
| Per-gun fire rate / reload | per gun in `guns.json` | `fire_delay`, `reload_time` | 20-TPS ceiling = 1 tick = 1200 RPM. |

### Speed Cola / Double Tap (fixed wiring)
- **Speed Cola** now correctly halves **reload** time (`speedColaReloadMultiplier`, default `0.5`).
- **Double Tap** now correctly speeds **fire rate** (`doubleTapFireMultiplier`, default `1.5`).

### Stuck-zombie fix
A zombie that makes no progress for `zombieStuckSeconds` (default **6**, set `0` to disable) is
teleported to a reachable spawn near a player — fixes the classic "1 zombie left wedged on
geometry" round hang. `zombieStuckMoveThreshold` (default 1.0) = blocks-moved that counts as
progress. **Crawlers are exempt** (they're meant to be slow).

---

## 2. Perks (new)

Buy at a Perk Machine sign exactly like the existing perks (the sign name-matches the perk).

| Perk | How it works | Config |
|---|---|---|
| **Deadshot Daiquiri** | Extra headshot damage on top of the base 1.5× | `config.perks.deadshotHeadshotMultiplier` = 2.0 |
| **Vulture Aid** | On your kills, a chance to drop ammo/points | `vultureDropChance` = 10(%), `vulturePointsDrop` = 25 |
| **Tombstone Soda** | Keep your perks when you go down; reclaimed on next-round revive (one-shot) | `config.perks.tombstoneEnabled` = true |

### Solo survivability
- **Solo Quick Revive self-revive** — in a 1-player game, Quick Revive auto-revives you a limited
  number of times. `config.perks.soloQuickReviveUses` = 3, `config.ReviveSettings.SoloReviveDelaySeconds` = 5.
- **Who's Who** (perk, solo, *simplified*) — on down you become a mobile glowing "ghost" with a
  pistol; return to your body within `whosWhoSeconds` (20) and `whosWhoReviveRange` (3.0) blocks to
  self-revive. (Bukkit has no real player-clone — the body is a marker, not a killable second
  entity. Takes precedence over solo Quick Revive.)

---

## 3. Power-ups (new)

Drop randomly from kills like the existing power-ups (enabled per-arena in `arenas.json`).

| Power-up | Effect | Config |
|---|---|---|
| **Bonus Points** | Flat points to every player | `bonusPointsAmount` = 100 |
| **Random Perk Bottle** | Grants the picker a random perk | (uses perk system) |
| **Death Machine** | Temporary minigun, then your weapons are restored | `deathMachineDurationSeconds` = 30 |
| **Bonfire Sale** | Fire Sale **+** Pack-a-Punch cost drops to a flat price | `bonfirePaPCost` = 1000 |

Also: power-ups now **cap at 4** on the ground (`maxPowerUpsOnGround`), re-picking a timed power-up
**refreshes** its timer (`powerUpRefreshOnPickup`), and re-Pack-a-Punching refills ammo for a fee
(`packAPunchRepackEnabled`, `packAPunchRepackCost` = 2500).

---

## 4. Enemies & rounds

### Crawler (last-zombie hold)
When a round is fully spawned and **one** zombie remains, it becomes a slow, gas-emitting
**crawler** (glowing, throws a brief poison cloud at nearby players) so you can hold the round.
Insta-Kill will **not** auto-kill the held crawler. Disable with `lastZombieCrawler: false`.
Tunables: `crawlerHealth` (2.0), `crawlerGasRadius` (3.0).
> This is the "weird particle zombie that throws poison" — it's intended; set `lastZombieCrawler: false` to remove it.

### Boss rounds (off by default)
Set `bossRoundEveryX` to a number (e.g. `5`) to enable a boss every Nth round (takes precedence
over dog rounds). Alternates:
- **George Romero** — very tanky, glowing, drops Max Ammo + a random perk on death.
- **Brutus** — armored; when he hits you he temporarily disables one of your perks
  (`brutusDisableSeconds` = 5).

`bossHealthMultiplier` (8.0) scales boss HP off the normal round curve. `bossRoundEveryX: 0` = off.

### Wonder weapons
Added to `guns.json` (box/wall via the usual weapon system):
- **Thundergun** — blasts zombies away with massive cone knockback (`config.wonderWeapons.thundergunKnockback`).
- **Wunderwaffe DG-2** — chain lightning across nearby zombies
  (`wunderwaffeChainCount` = 5, `wunderwaffeChainRadius` = 6.0).
- New roster guns: MSMC, Skorpion EVO, Vector K10, Peacekeeper, Mk 48.

---

## 5. New signs

All signs follow the plugin convention: line 1 = `[Zombies]`, line 2 = the keyword below. Place in
an arena (in setup) like any other game sign.

| Keyword | Sign | Use |
|---|---|---|
| `trap` | **Trap** | line 3 = id, line 4 = cost. Right-click to trigger a timed kill-zone around the sign. `config.trap.durationSeconds` (10), `cooldownSeconds` (60), `killRadius` (4.0). |
| `bank` | **Bank** | Right-click = deposit all points (fee `config.bank.depositFeePercent` = 10%); shift-right-click = withdraw. Persists across games. |
| `fridge` | **Fridge** | Right-click holding a gun = store it (name + Pack-a-Punch); shift-right-click = retrieve. One weapon, persists across games. |
| `buildable` | **Buildable station** | line 3 = buildable id (built-in: `zombie_shield`). Right-click empty-handed to buy a missing part (`config.buildable.partCost` = 500); right-click holding a part to deposit; assembles when complete. |
| `quest` | **Quest step** | line 3 = step id. Right-click to advance an `INTERACT` step of the arena's easter-egg quest. |

### Teleporters (enhanced)
Existing teleporter signs now have: a recharge cooldown (`config.teleporter.cooldownSeconds` = 30),
a charge-up delay (`chargeUpTicks` = 40), they **kill zombies on the pad** on use
(`padKillRadius` = 3.0), and play a sound (`sound: true`).

**Teleporter-gated Pack-a-Punch** (optional, backward-compatible):
- Make a teleporter grant timed PaP access: end its **label line** with `pap` (e.g. `Teleporter pap`).
  Using it grants `config.teleporter.paPAccessSeconds` (30) of PaP access.
- Gate a Pack-a-Punch sign behind it: put `tp` / `gated` / `teleporter` on **line 4**. Players must
  reach PaP via the teleporter. Unflagged teleporters/PaP signs behave exactly as before.

---

## 6. Persistence & progression

Stored in `plugins/COM_Zombies/playerdata.json` (Gson, keyed by player UUID).

- **Bank** — persistent points (see Bank sign).
- **Fridge** — one stored weapon between games (see Fridge sign).
- **Perma-perks** — earned by **reviving teammates** (lifetime count). Co-op only.
  - Perma Jugg (lite): unlock at `permaJuggReviveThreshold` (10) revives → 5s Regen on game entry.
  - Perma Quick Revive (lite): unlock at `permaQuickReviveThreshold` (15) → 5s Speed on game entry.
  - **Disable everything:** `config.permaPerks.enabled: false` (no tracking, no effects).

---

## 7. Easter-egg quest framework

A generic, per-arena multi-step objective engine. Authored entirely by data — no code:

1. Add a `quest` object to the arena in `arenas.json`:
   ```json
   "quest": {
     "id": "my_egg",
     "steps": [
       { "type": "INTERACT",    "param": "tower", "description": "Activate the tower" },
       { "type": "REACH_ROUND", "param": "10",    "description": "Survive to round 10" },
       { "type": "INTERACT",    "param": "valve", "description": "Turn the valve" }
     ]
   }
   ```
2. Place `[Zombies]` / `quest` signs in the world with the matching step id on line 3 (for
   `INTERACT` steps). `REACH_ROUND` steps advance automatically.
3. On completion every player is rewarded (`config.quest.completionReward` = 5000 points).

Single linear track; quest progress resets per game (only the definition persists).

---

## 8. Quick "turn it off" reference

| Want gone | Set |
|---|---|
| Last-zombie crawler | `lastZombieCrawler: false` |
| Boss rounds | `bossRoundEveryX: 0` (default) |
| Perma-perks | `config.permaPerks.enabled: false` |
| Stuck-zombie teleport | `zombieStuckSeconds: 0` |
| Dog Max-Ammo reward | `dogRoundMaxAmmoDrop: false` |
| PaP re-pack | `packAPunchRepackEnabled: false` |
| Power-up timer refresh | `powerUpRefreshOnPickup: false` |
| Vulture Aid drops | `vultureDropChance: 0` |

New signs/perks/power-ups only appear if you place the sign / make the perk buyable / enable the
power-up per arena — nothing forces them into an existing map.
