# Gun model mapping (BO2 roster → source pack models)

The plugin's roster is Black Ops 2; the source BOZ pack ships WaW + Black Ops 1 models.
Each gun is assigned a `comz:gun/<slug>` item model (set in `guns.json`). The build script
(`build_pack.py`) generates `assets/comz/items/gun/<slug>.json` pointing at a source model.

- **✓ direct/close** — the pack has the actual gun (or a same-name BO1 Pack-a-Punch model).
- **↦ fallback** — no BO2 model in the pack; pointed at the closest same-class WaW/BO1 model.
  Replace these with real BO2 models later: drop a Blockbench model at
  `assets/minecraft/models/custom/item/bo2/<slug>.json` (+ its texture) and repoint the
  slug's entry in `build_pack.py` to `custom/item/bo2/<slug>`.

| Gun (base → PaP) | slug | base model | PaP model | status |
|---|---|---|---|---|
| M1911 → C-3000 | `m1911` | `3d_guns/m1911_v2` | `1waw/pap/c_3000` | ✓ |
| Python → Cobra | `python` | `3d_guns/python` | `2blops/pap/cobra` | ✓ |
| Olympia → Hades | `olympia` | `2blops/olympia` | `2blops/pap/hades` | ✓ |
| Colt M16A1 → Skullcrusher | `m16` | `2blops/m16` | `2blops/pap/skullcrusher` | ✓ |
| FAL → EPC WN | `fal` | `3d_guns/fal` | `2blops/pap/epc_wn` | ✓ |
| M14 → Mnesia | `m14` | `2blops/m14` | `2blops/pap/mnesia` | ✓ |
| MP5 → MP115 Kollider | `mp5` | `2blops/mp5` | `2blops/pap/mp115` | ✓ |
| SVU-AS → Shadowy Veil | `svu` | `2blops/dragunov` | `2blops/dragunov` | ✓ (Dragunov family) |
| Ray Gun → Porter's X2 | `raygun` | `3d_guns/raygun_v2` | `wonder/pap/porter_x2` | ✓ |
| Thundergun → Zeus Cannon | `thundergun` | `wonder/thundergun` | `wonder/thundergun` | ✓ (reuse) |
| Wunderwaffe DG-2 → DG-3 JZ | `wunderwaffe` | `wonder/wunderwaffe` | `wonder/wunderwaffe` | ✓ (reuse) |
| B23R → B34R | `b23r` | `3d_guns/cz75` | `3d_guns/cz75` | ↦ |
| Executioner → Voice of Justice | `executioner` | `1waw/sawn_off` | `1waw/sawn_off` | ↦ |
| Five-Seven → Ultra | `five_seven` | `3d_guns/m1911_v2` | `3d_guns/m1911_v2` | ↦ |
| Kap-40 → KAP-4000 | `kap40` | `3d_guns/cz75` | `3d_guns/cz75` | ↦ |
| M1216 → Mesmerizer | `m1216` | `2blops/spas_12` | `2blops/spas_12` | ↦ |
| R870 MCS → R-870 MCS | `r870` | `2blops/stakeout` | `2blops/stakeout` | ↦ |
| S12 → Synthetic Dozen | `s12` | `2blops/spas_12` | `2blops/spas_12` | ↦ |
| AN-94 → Actuated Neutralizer | `an94` | `2blops/galil` | `2blops/galil` | ↦ |
| M8A1 → Micro Aerator | `m8a1` | `2blops/m16` | `2blops/m16` | ↦ |
| M27 → Mystifier | `m27` | `2blops/commando` | `2blops/commando` | ↦ |
| MTAR → Malevolent Taxonomic… | `mtar` | `2blops/galil` | `2blops/galil` | ↦ |
| SMR → SM1L3R | `smr` | `2blops/m14` | `2blops/m14` | ↦ |
| Type 25 → Strain 25 | `type25` | `2blops/commando` | `2blops/commando` | ↦ |
| HAMR → SLDG-HAMR | `hamr` | `2blops/hk21` | `2blops/hk21` | ↦ |
| LSAT → FSIRT | `lsat` | `2blops/hk21` | `2blops/hk21` | ↦ |
| RPD → Relativistic Punishment… | `rpd` | `2blops/rpk` | `2blops/rpk` | ↦ (RPD≈RPK) |
| Mk 48 → Mauler 48000 | `mk48` | `2blops/hk21` | `2blops/hk21` | ↦ |
| Chicom CQB → …Quadruple Burst | `chicom` | `2blops/mpl` | `2blops/mpl` | ↦ |
| PDW-57 → Predictive Death Wish | `pdw57` | `3d_guns/spectre` | `3d_guns/spectre` | ↦ |
| MSMC → Tika-Tovo | `msmc` | `2blops/pm63` | `2blops/pm63` | ↦ |
| Skorpion EVO → Scorpio Evolution | `skorpion` | `2blops/pm63` | `2blops/pm63` | ↦ |
| Vector K10 → Kannon-10000 | `vector` | `3d_guns/spectre` | `3d_guns/spectre` | ↦ |
| Peacekeeper → Peacekeeper MK2 | `peacekeeper` | `2blops/ak74u` | `2blops/ak74u` | ↦ |
| Barret 50KCAL → Macro Annihilator | `barret` | `2blops/l96` | `2blops/l96` | ↦ |
| DSR 50 → Dead Specimen Reactor | `dsr50` | `2blops/l96` | `2blops/l96` | ↦ |

**Perks** all use `custom/item/powerups/perk_bottle_base` (the pack has no per-perk bottle model;
add `custom/item/perks/<slug>` models later and repoint `PERK_MODEL`/per-slug in the script).
**Power-ups** map 1:1 to `custom/item/powerups/*` (Bonus Points → generic `powerup_base`).
**Mystery-box teddy** → `custom/item/teddies/box_teddy`.
