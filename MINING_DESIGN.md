# Mining Design — Phase 2.3b

This document is the locked-in design for the next, large PR (Phase
2.3b). It is the fully-worked-out plan for "блоки дают свои кусочки, а
не сами себя; собрать блок обратно — дороже" combined with the user's
24-category breakdown of which tier mines what.

Status: **approved with revisions** (user answered the 7 open issues
in §8 — see new §0 below for the locked-in changes). The implementation
PR will be built against the revised spec. The original 24-category
body (§4) is preserved for context; revisions are noted inline.

> The user's original 24-category list is preserved verbatim in
> `HISTORY.md` → "Phase 2.3b — Block shards + 24-category mining logic
> (in design)". This doc is Devin's expanded form, with the
> carve-outs / additions / coverage list applied.

---

## 0. User answers (locked-in revisions to this spec)

User responded to the 7 open issues in chat. Concretely:

### 0.1 Valuable ores get their own shard; non-valuable ores stay vanilla

Answer to Q1 from §8: **split the ore-shard families per-mineral, but
only for ores that produce a craft-critical resource**. The rest stay
at vanilla drops (excluded from the shard system; mining gate still
enforces the tier required to break them).

| Ore block | Vanilla drop | New shard | Mode |
|---|---|---|---|
| `iron_ore`, `deepslate_iron_ore` | `raw_iron` | `iron_fragment` | **replace** |
| `gold_ore`, `deepslate_gold_ore`, `nether_gold_ore` | `raw_gold` / `gold_nugget` | `gold_fragment` | **replace** |
| `emerald_ore`, `deepslate_emerald_ore` | `emerald` | `emerald_fragment` | **replace** |
| `diamond_ore`, `deepslate_diamond_ore` | `diamond` | `diamond_fragment` | **replace** |
| `nether_quartz_ore` | `quartz` | `quartz_fragment` | **replace** |
| `ancient_debris` | `ancient_debris` (block) | `ancient_fragment` | **replace** |
| `coal_ore`, `deepslate_coal_ore` | `coal` | — | **excluded** (vanilla) |
| `copper_ore`, `deepslate_copper_ore` | `raw_copper` | — | **excluded** (vanilla) |
| `lapis_ore`, `deepslate_lapis_ore` | `lapis_lazuli` | — | **excluded** (vanilla) |
| `redstone_ore`, `deepslate_redstone_ore` | `redstone` | — | **excluded** (vanilla) |

This effectively replaces §4's `BASIC_ORE` (#9), `METAL_ORE` (#10),
`RARE_ORE` (#11), and `ANCIENT_MATERIAL` (#12) with **6 distinct shard
items** plus 4 excluded ore families. The user-facing rule for the
implementation:

- 4 `iron_fragment` → 1 `raw_iron` (and similarly for each shard).
- 8 `iron_fragment` → 1 `iron_ore` (block, for collectors).
- Ditto for gold / emerald / diamond / quartz / ancient_debris.
- Coal / copper / lapis / redstone — vanilla drops, no recombine
  recipe needed.

Note: `ancient_fragment` still also covers `obsidian`, `crying_obsidian`,
`respawn_anchor` (not just ancient_debris), since those are the other
diamond-tier mining targets in the user's #12 category. These are
*not* ore-derived; they're the "end-game stone" group.

### 0.2 Simplify decorative / non-essential blocks ("только важные")

Answer to Q2 from §8 + user's general guidance "можешь пропускать
предметы которые не очень важные для крафтов или прохождения игры,
только важные блоки и ресурсы".

Devin's interpretation: shard system applies to **important** blocks
(structural, ores, resources). Decorative or one-off blocks fall back
to vanilla drops with mining-gate-only enforcement.

**Excluded from shard system (vanilla drops, gate still applies):**

- 🌊 `sponge`, `wet_sponge` — niche, vanilla self-drop.
- 🐝 `hay_block`, `dried_kelp_block`, `honey_block`, `honeycomb_block`,
  `slime_block` — niche, vanilla self-drop.
- 🐸 `frogspawn`, `pearlescent_froglight`, `verdant_froglight`,
  `ochre_froglight` — niche light/decor, vanilla self-drop.
- 💀 `skeleton_skull`, `wither_skeleton_skull`, `zombie_head`,
  `creeper_head`, `dragon_head`, `piglin_head`, `player_head` —
  trophies, vanilla self-drop.
- 🪟 `cake` — vanilla self-eaten, no drop, leave as-is.
- 🪸 `coral`, `coral_block`, `coral_fan`, `dead_coral*` — vanilla
  drops (alive coral disappears without silk; dead self-drops). Don't
  add shards.
- 🛏️ `bed` (16 colors) — additive: vanilla bed drop + 2 `cloth_piece`
  bonus (so beds don't disappear from looted villages). User's #17
  WOOL had beds in replace mode; flip to additive here.
- 🪖 `banner` (16 colors), `wall_banner` — vanilla self-drop with NBT
  preserved (banner patterns matter).
- 📦 `shulker_box` (16 colors) — **excluded** (Q3 answer (а)). Vanilla
  self-drop with NBT (inventory inside is preserved).
- 🔥 `torch`, `soul_torch`, `redstone_torch` — vanilla self-drop
  (originally #23 LIGHT_FAMILY in replace mode; reclassify to vanilla
  self-drop; placed torches are just lost like vanilla).
- 🕯️ `candle` (and 16 colored variants) — vanilla self-drop.
- 🎯 `target` — vanilla self-drop.
- 📜 `lectern`, `loom`, `cartography_table`, `fletching_table`,
  `smithing_table`, `composter`, `note_block`, `jukebox`,
  `bookshelf`, `chiseled_bookshelf` — these are stations but **not**
  the BTW-style commitment ones. Replace mode → `wood_chip` shards
  (4 chips → 1 station, see §0.4 for cost). User's #6 WOOD_FAMILY
  list includes them.
- 🪞 `flower_pot` (with or without flower), `decorated_pot` — vanilla
  self-drop (decorative).
- 🌫️ `glow_lichen` — additive (already listed in #0 ORGANIC_SOFT;
  remove from #23 LIGHT_FAMILY).

**Promoted to SPECIAL_UNBREAKABLE (cannot be mined at any tier):**

- `budding_amethyst` (Q2 answer (а)) — vanilla deletes on break;
  preserve that.
- `mob_spawner` (Q4 answer) — vanilla pickaxe-mineable but drops
  nothing; preserve that.
- `dragon_egg` — vanilla teleports on hit; preserve that.
- `sculk_sensor`, `calibrated_sculk_sensor`, `sculk_shrieker`,
  `sculk_catalyst` — Deep Dark mechanics; preserve their detection
  behavior, don't let players relocate them.
- `sculk` (block), `sculk_vein` — Deep Dark; preserve.
- (All the existing UNBREAKABLE entries: bedrock, command_block,
  end_portal_frame, barrier, structure_block, light_block, jigsaw,
  end_portal, end_gateway, fire, soul_fire, lava, water,
  nether_portal, moving_piston, piston_head.)

### 0.3 Workshop stations cost double (8 shards)

Answer to Q4 from §8: **8 shards** for the BTW-style commitment
stations. List:

```
crafting_table, chest, trapped_chest, barrel,
furnace, smoker, blast_furnace,
anvil, chipped_anvil, damaged_anvil,
brewing_stand, grindstone
```

All other recombines stay at the default 4 shards → 1 block.

### 0.4 Recipe advancements deferred

Answer to Q5: **defer to a separate clean-up PR** (which also resolves
the pre-existing missing-advancements debt from Phases 2.0–2.2.y).
The Phase 2.3b implementation PR will *not* generate advancement JSONs
— recipes are craftable but not auto-unlocked in the recipe book.

### 0.5 Rename `plant_fiber` → `organic_fiber`

Answer to Q6 (а): **rename**. Implementation:

- Rename the registered item ID from `glebthanwolves:plant_fiber` to
  `glebthanwolves:organic_fiber`.
- Add `MissingMappings` handler so old saves with `plant_fiber` stacks
  resolve to `organic_fiber` automatically (no item loss).
- Update the `plant_cordage` recipe input from `plant_fiber` →
  `organic_fiber`.
- Update lang strings (en + ru) and model JSON.
- Update the bushcraft GLM that drops fiber off grass (Phase 1.1).

### 0.6 Organic plants in additive mode

Answer to Q7 (б): **additive**. Plants and food blocks drop their
vanilla items AND 2 `organic_fiber` extra. No per-plant carve-outs
beyond the crops listed in §4 #0.

### 0.7 Final shard inventory

After §0 revisions, the implementation ships **24 shard items**
(originally planned was 24; net change is 0 because we split 4 ore
shards into 6 valuable-ore shards but lose 4 categories' shards
through exclusion):

```
organic_fiber          (renamed from plant_fiber; ORGANIC_SOFT additive)
leaf_fragment          (LEAF_FAMILY additive)
dirt_chunk             (DIRT_FAMILY replace)
sand_pile              (SAND_FAMILY replace)
gravel_piece           (GRAVEL_FAMILY additive)
snow_chunk             (SNOW_FAMILY replace)
wood_chip              (WOOD_FAMILY replace)
stone_fragment         (SOFT_STONE replace)
deepstone_fragment     (HARD_STONE replace)
iron_fragment          (iron ore, replace)
gold_fragment          (gold + nether_gold, replace)
emerald_fragment       (emerald, replace)
diamond_fragment       (diamond, replace)
quartz_fragment        (nether_quartz, replace)
ancient_fragment       (ancient_debris + obsidian + crying_obsidian + respawn_anchor, replace)
nether_fragment        (NETHER_STONE replace)
brick_fragment         (DECORATIVE_BLOCKS replace, narrowed)
hard_brick_fragment    (HARD_DECORATIVE replace, narrowed)
glass_shard            (GLASS_FAMILY replace)
cloth_piece            (WOOL_FAMILY replace, beds excluded)
ceramic_piece          (CERAMIC_FAMILY replace, narrowed)
concrete_dust          (CONCRETE_FAMILY replace)
compressed_metal_fragment  (METAL_BLOCKS replace)
precious_fragment      (PRECIOUS_BLOCKS replace)
machine_scrap          (MACHINE_FAMILY replace, narrowed)
```

(This is 25 entries; `organic_fiber` is the renamed `plant_fiber`, so
the number of *new* items added by Phase 2.3b is 24.)

Deleted from the original spec:
- `ore_fragment` (BASIC_ORE) — coal/copper now excluded
- `metal_fragment` (METAL_ORE) — only iron stays as own shard;
  lapis/redstone excluded
- `rare_fragment` (RARE_ORE) — split into 4 (gold/emerald/diamond/
  quartz) + ancient remains
- `light_fragment` (LIGHT_FAMILY) — torches/candles now excluded
  (vanilla self-drop)

---

## 1. Goals

1. **Blocks become a finite resource.** Mining a block does not give
   you the block; it gives 2 fragments (shards). Reassembling a block
   needs 4 fragments. So every transplant of a block from cave to
   surface costs 50%.
2. **Tiered access.** A first-day player breaks plants and dirt with
   their hand; rocks need wood-tier; deepslate / ores need stone-tier;
   rare ores / machines need iron-tier; obsidian / debris need
   diamond-tier. Same as the user's list.
3. **No soft-locks.** Food chains, sapling regen, primitive_axe
   crafting on spawn must keep working. Concrete carve-outs below.
4. **Mining gate stays default-deny.** Phase 2.3a already enforces
   "no block breaks unless its category is whitelisted by tier." This
   PR fills the whitelist.
5. **Tool *type* matters for drops, not only for break-speed.**
   Vanilla rule: pickaxe-only for ore drops; axe-only for log drops
   (with quirk that some logs drop without an axe). We honor this:
   wrong tool type ⇒ no drop, even at correct tier. Same as the
   user's "лопата ломает руду, но руда не выпадает от лопаты".

---

## 2. Architecture

### 2.1 Mining gate (Phase 2.3a, recap)

`events/MiningGate.java` already exists and:

- On `PlayerEvent.BreakSpeed`: returns 0 if the held tool's tier
  level (`Tier.getLevel()` or 0 for hand) is below the block's
  required tier. The block visibly takes infinite time to break.
- On `BlockEvent.BreakEvent`: cancels as defense-in-depth.

Phase 2.3a shipped this with **empty whitelists**. The gate uses 6
tag files: `breakable_by/{hand,primitive,stone,iron,diamond,
netherite}.json`. Each higher tag transitively includes the lower
ones, so adding a block to e.g. `stone.json` unlocks it for stone /
iron / diamond / netherite tiers automatically.

### 2.2 Per-category shard logic (this PR)

A new global loot modifier (GLM) called `BlockShardModifier` is
registered. It runs after the vanilla loot table for any block in
the GTW shard system. Its logic per block:

```
if held tool has Silk Touch enchantment:
    return vanilla_drops              # bypass — Q6 carve-out
elif block is in ADDITIVE category (LEAVES, GRAVEL, ORGANIC):
    return vanilla_drops + [shard × 2]
elif block is in EXCLUDED category (crops, suspicious_*, etc.):
    return vanilla_drops
else:                                 # REPLACE category
    if held tool type does not match category requirement:
        return []                     # wrong tool, nothing drops
    else:
        return [shard × 2]            # replace vanilla drops entirely
```

**Tool type checking** is done by inspecting the held `ItemStack`'s
class:

| Required type | Matches Java classes |
|---|---|
| `any` | (always matches) |
| `axe` | `AxeItem` |
| `pickaxe` | `PickaxeItem` |
| `shovel` | `ShovelItem` |
| `shears` | `ShearsItem` |
| `hoe` | `HoeItem` |
| `sword` | `SwordItem` (cobwebs only) |
| `hand` | empty stack OR none of the above |

Vanilla GTW tools and vanilla tools both share these base classes,
so the check works for both. (Vanilla tools are still bannable via
Phase 2.7 structure-loot replacement — orthogonal to this PR.)

### 2.3 Recombine recipes

Default: 4 shards → 1 block (50% loss). Specific recipes laid out
per category in §4 below. Some categories produce multiple targets
(e.g. `metal_fragment` can recombine into raw iron, lapis, or
redstone — player picks the recipe).

---

## 3. Tiers and tool types

Five **tiers**:

| # | Tier | GTW tools | Found vanilla |
|---|---|---|---|
| 0 | hand | (nothing held, or non-tool) | — |
| 1 | wood (primitive+wooden) | `primitive_axe`, `wooden_pickaxe`, `wooden_axe`, `wooden_sword`, `wooden_shovel`, `wooden_hoe` | `wooden_*` (all 5) |
| 2 | stone | `stone_pickaxe`, `stone_axe`, `stone_sword`, `stone_shovel`, `stone_hoe` | `stone_*`, `golden_*` (golden = level 1, but vanilla tier 0 in some classes — confirm in implementation) |
| 3 | iron | `iron_pickaxe`, `iron_axe`, `iron_sword`, `iron_shovel`, `iron_hoe` | `iron_*` |
| 4 | diamond | `diamond_pickaxe`, `diamond_axe`, `diamond_sword`, `diamond_shovel`, `diamond_hoe` | `diamond_*`, `netherite_*` (netherite = level 4) |

(Note: `Tier.getLevel()` for vanilla `WOOD = 0`, `STONE = 1`, `IRON
= 2`, `DIAMOND = 3`, `NETHERITE = 4`. GTW mirrors these levels.
In this doc, "tier #1" means "level 0" = wood. The user's
categories use the same convention.)

The **netherite** tier is reserved (Phase 2.3a tag exists, kept
empty). Diamond is the practical ceiling for now.

---

## 4. The 24 categories

Each category section is structured as:

> **#N — NAME** *(drop, tier, tool type, mode)*
> *Mode = `replace` (drops swap to shard), `additive` (vanilla + shard), `excluded` (vanilla only, no shard).*
> Block list; recombine recipe(s); notes.

### #0 — ORGANIC_SOFT *(drop: `organic_fiber`, tier: hand, tool: any, mode: **additive**)*

Mode is **additive**, not replace. Vanilla drops (saplings, food,
seeds, wheat, mushrooms, sugar cane, etc.) are preserved; the block
ALSO drops 2 `organic_fiber`. Without this, every soft plant becomes
a soft-lock vector (no food, no industry, no saplings).

This means `organic_fiber` overlaps with the existing
`plant_fiber` from Phase 1.1. Decision: **`organic_fiber` is the
new canonical name** and `plant_fiber` is renamed to
`organic_fiber` in this PR. Old `plant_cordage` recipe migrated.

Blocks (additive):

```
short_grass, tall_grass, fern, large_fern,
dandelion, poppy, blue_orchid, allium, azure_bluet,
red_tulip, orange_tulip, white_tulip, pink_tulip,
oxeye_daisy, cornflower, lily_of_the_valley, wither_rose,
sunflower, lilac, rose_bush, peony,
torchflower, pitcher_plant, pink_petals,
dead_bush, vine, glow_lichen, moss_carpet, moss_block,
lily_pad, hanging_roots, spore_blossom,
small_dripleaf, big_dripleaf, big_dripleaf_stem,
cave_vines, cave_vines_plant,
twisting_vines, twisting_vines_plant,
weeping_vines, weeping_vines_plant,
chorus_flower, chorus_plant,
sweet_berry_bush,
kelp, kelp_plant, seagrass, tall_seagrass,
sugar_cane, bamboo, bamboo_sapling, cactus,
brown_mushroom, red_mushroom,
crimson_roots, warped_roots, nether_sprouts, nether_wart
```

**Excluded entirely (no shard at all, vanilla drops only — these
are crops):**

```
wheat, carrots, potatoes, beetroot,
pumpkin_stem, attached_pumpkin_stem,
melon_stem, attached_melon_stem
```

(User confirmed: "ты прав с едой ошибка" — variant (a).)

Recombine: none — `organic_fiber` is consumed by the existing
`plant_cordage` recipe (4 fiber → 1 cordage).

### #1 — LEAF_FAMILY *(drop: `leaf_fragment`, tier: hand, tool: any, mode: **additive**)*

Additive — vanilla saplings, apples, sticks all keep dropping.
Otherwise, sapling regen breaks → no trees → soft-lock.

Blocks:

```
oak_leaves, spruce_leaves, birch_leaves, jungle_leaves,
acacia_leaves, dark_oak_leaves, mangrove_leaves,
cherry_leaves, azalea_leaves, flowering_azalea_leaves,
nether_wart_block, warped_wart_block
```

Recombine: 4 `leaf_fragment` → 1 `oak_leaves` (decoration recipe).
(Or generic: `leaf_fragment` is also crafting input for compost-type
items — TBD in a later phase. For now, 1 generic recombine.)

### #2 — DIRT_FAMILY *(drop: `dirt_chunk`, tier: hand, tool: any, mode: **replace**)*

Blocks:

```
dirt, grass_block, coarse_dirt, rooted_dirt,
podzol, mycelium, mud, muddy_mangrove_roots,
clay, packed_mud, farmland, dirt_path
```

Note on **clay**: vanilla drops 4 `clay_ball`. Replace mode means
clay → 2 `dirt_chunk`. Recipe lets you craft 4 `dirt_chunk` → 4
`clay_ball` (50% loss vs vanilla — user's "крутая потеря" applies).

Note on **farmland / dirt_path**: vanilla drops `dirt`. Replace mode
means they drop `dirt_chunk × 2`. Same recombine as dirt.

Recombines:
- 4 `dirt_chunk` → 1 `dirt`
- 4 `dirt_chunk` → 4 `clay_ball`
- 4 `dirt_chunk` → 1 `coarse_dirt` (variant)
- 4 `dirt_chunk` + 1 `wheat_seeds` → 1 `farmland` (decorative re-craft)

### #3 — SAND_FAMILY *(drop: `sand_pile`, tier: hand, tool: shovel-or-any, mode: **replace**)*

Blocks:

```
sand, red_sand, soul_sand, soul_soil
```

**Excluded:** `suspicious_sand` (archaeology — vanilla brushing
behavior preserved).

Recombines:
- 4 `sand_pile` → 1 `sand`
- 4 `sand_pile` → 1 `red_sand`
- 4 `sand_pile` → 1 `soul_sand`
- 4 `sand_pile` → 1 `soul_soil`

### #4 — GRAVEL_FAMILY *(drop: `gravel_piece`, tier: hand, tool: any, mode: **additive**)*

Additive — vanilla `flint` (10% drop) preserved. Otherwise
`primitive_axe` (flint + stick + cordage) becomes uncraftable.

Blocks:

```
gravel
```

**Excluded:** `suspicious_gravel`.

Recombines:
- 4 `gravel_piece` → 1 `gravel`

### #5 — SNOW_FAMILY *(drop: `snow_chunk`, tier: hand, tool: shovel-or-any, mode: **replace**)*

Blocks:

```
snow, snow_block, powder_snow, ice, packed_ice, blue_ice
```

**Note on ice:** vanilla mining of ice with a non-silk pickaxe turns
it to water (no item drop). With our system, ice → `snow_chunk × 2`.
Semantically a stretch, but it's the user's chosen design (ice is in
SNOW_FAMILY, drop is `snow_chunk`). Silk-touch carve-out preserves
ice→ice block.

Recombines:
- 4 `snow_chunk` → 1 `snow_block`
- 4 `snow_chunk` → 4 `snowball`
- 4 `snow_chunk` → 1 `ice` (re-creates ice from compressed snow, fits
  the "frozen water" narrative)
- 4 `snow_chunk` → 1 `packed_ice`
- 4 `snow_chunk` → 1 `blue_ice`

### #6 — WOOD_FAMILY *(drop: `wood_chip`, tier: wood, tool: axe, mode: **replace**)*

Tool type is **axe** specifically (AxeItem). Pickaxe / shovel /
sword / hoe at wood-tier+ can break wood blocks (mining gate allows
the tier), but they get nothing — only an axe extracts the chip.

Blocks (logs / planks / stripped variants / wooden building blocks):

```
oak_log, spruce_log, birch_log, jungle_log, acacia_log,
dark_oak_log, mangrove_log, cherry_log,
oak_wood, spruce_wood, birch_wood, jungle_wood, acacia_wood,
dark_oak_wood, mangrove_wood, cherry_wood,
stripped_oak_log, stripped_spruce_log, stripped_birch_log,
stripped_jungle_log, stripped_acacia_log, stripped_dark_oak_log,
stripped_mangrove_log, stripped_cherry_log,
stripped_oak_wood, stripped_spruce_wood, stripped_birch_wood,
stripped_jungle_wood, stripped_acacia_wood, stripped_dark_oak_wood,
stripped_mangrove_wood, stripped_cherry_wood,
crimson_stem, warped_stem, crimson_hyphae, warped_hyphae,
stripped_crimson_stem, stripped_warped_stem,
stripped_crimson_hyphae, stripped_warped_hyphae,
bamboo_block, stripped_bamboo_block,

oak_planks, spruce_planks, birch_planks, jungle_planks,
acacia_planks, dark_oak_planks, mangrove_planks, cherry_planks,
crimson_planks, warped_planks, bamboo_planks, bamboo_mosaic,

# wood family slabs / stairs / fences / fence_gates / doors /
# trapdoors / pressure_plates / buttons / signs / hanging_signs
# (full set per wood type — too many to list inline; expand by
# enumerating all `*_slab/_stairs/_fence/_fence_gate/_door/
# _trapdoor/_pressure_plate/_button/_sign/_hanging_sign/_wall_sign/
# _wall_hanging_sign` blocks for each of the 12 wood types).

ladder, scaffolding,
crafting_table, cartography_table, fletching_table, smithing_table,
loom, lectern, jukebox, note_block,
chest, trapped_chest, barrel,
bookshelf, chiseled_bookshelf,
composter,
beehive, bee_nest,

# user "обходы":
mushroom_stem, red_mushroom_block, brown_mushroom_block,
azalea, flowering_azalea,

# pumpkin family — wood-axe per vanilla
pumpkin, carved_pumpkin, jack_o_lantern, melon
```

**Excluded (entity, not block):**

```
oak_boat, spruce_boat, birch_boat, jungle_boat, acacia_boat,
dark_oak_boat, mangrove_boat, cherry_boat, bamboo_raft
```

Boats live as entities once placed. They cannot be mined, only
crafted. Listing them in the user's WOOD_FAMILY was a craft-side
concern; we handle that separately by leaving the vanilla boat
recipes alone (they still need wood, which is now scarce, so boats
become naturally rare).

Recombines (per wood type):
- 4 `wood_chip` → 1 `oak_log` (or any other log, planks, etc.)
- 4 `wood_chip` → 4 `oak_planks` (more efficient when you only need
  planks)
- 4 `wood_chip` → 1 `crafting_table` (replace mode means once you
  break your crafting table you must spend chips to make a new one;
  user accepted the BTW-style "stations are precious" angle)
- 4 `wood_chip` → 1 `chest`
- 4 `wood_chip` → 1 `barrel`
- … one recipe per recoverable wood block type.

(All recipes use wood_chip as the *only* material — wood type is
chosen by the recipe selected, not by the chips. This is the
"universal shard within a family" approach the user stated for
cobblestone family.)

### #7 — SOFT_STONE *(drop: `stone_fragment`, tier: wood, tool: pickaxe, mode: **replace**)*

Tool type is **pickaxe**. Other wood-tier tools (axe, sword, etc.)
can BREAK the block (gate accepts the tier) but get nothing.

Blocks:

```
stone, cobblestone, mossy_cobblestone, smooth_stone,
stone_bricks, cracked_stone_bricks, chiseled_stone_bricks,
mossy_stone_bricks, infested_stone, infested_cobblestone,
infested_stone_bricks, infested_mossy_stone_bricks,
infested_cracked_stone_bricks, infested_chiseled_stone_bricks,
granite, polished_granite,
diorite, polished_diorite,
andesite, polished_andesite,
tuff, calcite, dripstone_block,
pointed_dripstone,

# Stairs / slabs / walls of every block above. Enumerate in code:
# stone_stairs, stone_slab, stone_brick_stairs, stone_brick_slab,
# stone_brick_wall, mossy_stone_brick_*, granite_stairs/slab/wall,
# polished_granite_stairs/slab, diorite_stairs/slab/wall,
# polished_diorite_stairs/slab, andesite_stairs/slab/wall,
# polished_andesite_stairs/slab, cobblestone_stairs/slab/wall,
# mossy_cobblestone_stairs/slab/wall, smooth_stone_slab.

# user "обходы": furnace family
furnace, smoker, blast_furnace
```

Recombines:
- 4 `stone_fragment` → 1 `stone`
- 4 `stone_fragment` → 1 `cobblestone`
- 4 `stone_fragment` → 1 `granite` / `diorite` / `andesite` / `tuff`
  / `calcite` / `dripstone_block`
- 4 `stone_fragment` → 1 `furnace` (BTW commitment)
- 4 `stone_fragment` → 1 `smoker` / `blast_furnace`
- 4 `stone_fragment` → 1 `*_stairs` / `*_slab` (each variant)
- 4 `stone_fragment` → 1 `*_wall`

### #8 — HARD_STONE *(drop: `deepstone_fragment`, tier: stone, tool: pickaxe, mode: **replace**)*

Blocks:

```
deepslate, cobbled_deepslate, polished_deepslate, chiseled_deepslate,
deepslate_tiles, cracked_deepslate_tiles,
deepslate_bricks, cracked_deepslate_bricks,
infested_deepslate, reinforced_deepslate,

blackstone, polished_blackstone, chiseled_polished_blackstone,
polished_blackstone_bricks, cracked_polished_blackstone_bricks,
gilded_blackstone,

basalt, smooth_basalt, polished_basalt,
end_stone, end_stone_bricks,

# stairs / slabs / walls for all of the above.
# deepslate_*_stairs/slab/wall (full set),
# polished_blackstone_*, blackstone_*, basalt is no slab/stair set,
# end_stone_brick_stairs/slab/wall.
```

Recombines: 4 `deepstone_fragment` → 1 of the listed blocks (player
picks recipe).

### #9 — BASIC_ORE — REVISED IN §0.1: excluded entirely

**Per §0.1:** coal_ore / copper_ore are **excluded** from the shard
system. They keep vanilla drops (`coal`, `raw_copper`). Mining gate
still enforces wood-pickaxe-tier to break them.

Blocks (mining-gate `breakable_by/primitive` only):

```
coal_ore, deepslate_coal_ore,
copper_ore, deepslate_copper_ore
```

No `ore_fragment` item is created. The original recombine recipes
listed here are deleted.

### #10 — METAL_ORE — REVISED IN §0.1: split per-ore

**Per §0.1:**
- `iron_ore`, `deepslate_iron_ore` → `iron_fragment × 2` (replace).
  Tier: stone pickaxe. Mining-gate tag: `has_iron_fragment`.
  Recombines:
    - 4 `iron_fragment` → 1 `raw_iron`
    - 8 `iron_fragment` → 1 `iron_ore` (block; for collectors)
- `lapis_ore`, `deepslate_lapis_ore` → **excluded**. Vanilla
  `lapis_lazuli` drop. Tier: stone pickaxe.
- `redstone_ore`, `deepslate_redstone_ore` → **excluded**. Vanilla
  `redstone` drop. Tier: stone pickaxe (same loosening as before; the
  vanilla rule of "iron pickaxe to drop redstone" is dropped because
  our gate already enforces tier separately).

No `metal_fragment` item is created. The fungibility issue from the
original spec is moot.

### #11 — RARE_ORE — REVISED IN §0.1: split into 4 per-ore shards

**Per §0.1:**
- `gold_ore`, `deepslate_gold_ore`, `nether_gold_ore` → `gold_fragment
  × 2`. Tier: iron pickaxe (vanilla baseline). Recombines:
    - 4 `gold_fragment` → 1 `raw_gold`
    - 8 `gold_fragment` → 1 `gold_ore` (block)
- `emerald_ore`, `deepslate_emerald_ore` → `emerald_fragment × 2`.
  Recombines: 4 → 1 `emerald`; 8 → 1 `emerald_ore` (block).
- `diamond_ore`, `deepslate_diamond_ore` → `diamond_fragment × 2`.
  Recombines: 4 → 1 `diamond`; 8 → 1 `diamond_ore` (block).
- `nether_quartz_ore` → `quartz_fragment × 2`. Recombines: 4 → 1
  `quartz`; 8 → 1 `nether_quartz_ore` (block). Tier: iron pickaxe.

4 shard items instead of 1. No fungibility — mining gold gives gold
fragments only, etc.

### #12 — ANCIENT_MATERIAL *(drop: `ancient_fragment`, tier: diamond, tool: pickaxe, mode: **replace**)*

Kept as-is. Per §0.1 the `ancient_fragment` shard now also covers the
`ancient_debris` block (since coal/copper/lapis/redstone are excluded
but ancient_debris is in the user's diamond-tier valuable-ore list).

Blocks:

```
ancient_debris, obsidian, crying_obsidian, respawn_anchor
```

Recombines:
- 4 `ancient_fragment` → 1 `ancient_debris` (block)
- 4 `ancient_fragment` → 1 `obsidian`
- 4 `ancient_fragment` → 1 `crying_obsidian`
- 8 `ancient_fragment` → 1 `respawn_anchor`

(Note: user did NOT add netherite_ingot here — to make a netherite
ingot, the player still needs the vanilla path: smelt ancient_debris
into netherite_scrap, combine 4 scrap + 4 gold → ingot. We don't
short-circuit it.)

### #13 — NETHER_STONE *(drop: `nether_fragment`, tier: stone, tool: pickaxe, mode: **replace**)*

Blocks:

```
netherrack,
nether_bricks, cracked_nether_bricks, chiseled_nether_bricks,
red_nether_bricks,
magma_block,

# stairs / slabs / walls of nether_bricks, red_nether_bricks
```

Recombines: 4 `nether_fragment` → any of the above (player picks).

### #14 — DECORATIVE_BLOCKS *(drop: `brick_fragment`, tier: wood, tool: pickaxe, mode: **replace**)*

Blocks:

```
bricks, mud_bricks, packed_mud,                    # mud listed in DIRT — reconcile
sandstone, cut_sandstone, chiseled_sandstone, smooth_sandstone,
red_sandstone, cut_red_sandstone, chiseled_red_sandstone, smooth_red_sandstone,

prismarine, prismarine_bricks, dark_prismarine,

# stairs / slabs / walls of all of the above.
```

**Conflict with #2 DIRT:** `packed_mud` and `mud_bricks` were placed
by the user in both #2 and #14. Resolution: `packed_mud` stays in #2
(it's a dirt-derived block), `mud_bricks` stays in #14 (a built /
fired construction).

Recombines: 4 `brick_fragment` → 1 of any listed.

### #15 — HARD_DECORATIVE *(drop: `hard_brick_fragment`, tier: iron, tool: pickaxe, mode: **replace**)*

Blocks:

```
quartz_block, smooth_quartz, chiseled_quartz_block,
quartz_pillar, quartz_bricks,
purpur_block, purpur_pillar,
sea_lantern, glowstone,
amethyst_block,

# stairs / slabs of quartz, purpur (purpur_pillar has no slab/stair),
# smooth_quartz_stairs/slab.
```

Recombines: 4 `hard_brick_fragment` → 1 of any listed.

### #16 — GLASS_FAMILY *(drop: `glass_shard`, tier: wood, tool: pickaxe, mode: **replace**)*

Blocks:

```
glass, tinted_glass,
white_stained_glass, orange_stained_glass, magenta_stained_glass,
light_blue_stained_glass, yellow_stained_glass, lime_stained_glass,
pink_stained_glass, gray_stained_glass, light_gray_stained_glass,
cyan_stained_glass, purple_stained_glass, blue_stained_glass,
brown_stained_glass, green_stained_glass, red_stained_glass,
black_stained_glass,

glass_pane, white_stained_glass_pane,           # … and all 16 colors
… (full set of 16 colored panes)
```

**Note:** vanilla glass is hand-mineable but drops nothing without
silk-touch. With this system, glass becomes wood-pickaxe-tier (a
*tightening*) and drops 2 `glass_shard`. This is the user's
intentional choice (user listed glass under "ДЕРЕВЯННАЯ КИРКА").

Recombines:
- 4 `glass_shard` → 1 `glass`
- 4 `glass_shard` → 1 colored stained glass (one recipe per color,
  needs the dye too)
- 4 `glass_shard` → 16 `glass_pane`

### #17 — WOOL_FAMILY *(drop: `cloth_piece`, tier: hand, tool: shears-or-any, mode: **replace**)*

Blocks:

```
white_wool, orange_wool, magenta_wool, light_blue_wool, yellow_wool,
lime_wool, pink_wool, gray_wool, light_gray_wool, cyan_wool,
purple_wool, blue_wool, brown_wool, green_wool, red_wool, black_wool,

# carpets (16 colors)
white_carpet, orange_carpet, … black_carpet,

# beds (16 colors)
white_bed, orange_bed, … black_bed,

# banners (16 colors) — added by Devin (not in user's list, but
# wool/stick crafted, fits the family)
white_banner, … black_banner,
white_wall_banner, … black_wall_banner,
```

Recombines:
- 4 `cloth_piece` → 1 white_wool (or colored wool with dye)
- 4 `cloth_piece` → 1 carpet
- 4 `cloth_piece` + 1 `wood_chip` → 1 bed (cheaper than vanilla's 3
  wool + 3 plank because we already pay 50% loss)
- 4 `cloth_piece` + 1 `stick` → 1 banner

### #18 — CERAMIC_FAMILY *(drop: `ceramic_piece`, tier: wood, tool: pickaxe, mode: **replace**)*

Blocks:

```
terracotta,
white_terracotta, orange_terracotta, magenta_terracotta,
light_blue_terracotta, yellow_terracotta, lime_terracotta,
pink_terracotta, gray_terracotta, light_gray_terracotta,
cyan_terracotta, purple_terracotta, blue_terracotta,
brown_terracotta, green_terracotta, red_terracotta, black_terracotta,

# glazed terracotta (16 colors)
white_glazed_terracotta, … black_glazed_terracotta,

flower_pot, decorated_pot
```

**Conflict:** `flower_pot` is hand-mineable in vanilla, not pickaxe.
Resolution: keep flower_pot in CERAMIC (user's call) but at hand
tier with `tool: any` exception. Implementation: tag-based override.

Recombines: 4 `ceramic_piece` → 1 terracotta (color requires dye
input). 4 `ceramic_piece` → 1 flower_pot. 4 `ceramic_piece` → 1
decorated_pot (no sherds; decoration baseline).

### #19 — CONCRETE_FAMILY *(drop: `concrete_dust`, tier: stone, tool: pickaxe, mode: **replace**)*

Blocks:

```
white_concrete, orange_concrete, … black_concrete,           # 16 colors
white_concrete_powder, orange_concrete_powder, … black_concrete_powder
```

**Note:** `concrete_powder` is sand-physics (falls when unsupported).
Vanilla allows shovel-only drops. Devin's call: still pickaxe-tier
in line with user's grouping.

Recombines: 4 `concrete_dust` → 1 concrete (color requires dye
input). 4 `concrete_dust` → 1 concrete_powder.

### #20 — METAL_BLOCKS *(drop: `compressed_metal_fragment`, tier: iron, tool: pickaxe, mode: **replace**)*

Blocks:

```
iron_block, gold_block, copper_block,
raw_iron_block, raw_gold_block, raw_copper_block,

# Devin additions (logically part of the family):
exposed_copper, weathered_copper, oxidized_copper,
waxed_copper_block, waxed_exposed_copper, waxed_weathered_copper,
waxed_oxidized_copper,
cut_copper, exposed_cut_copper, weathered_cut_copper, oxidized_cut_copper,
waxed_cut_copper, waxed_exposed_cut_copper, waxed_weathered_cut_copper,
waxed_oxidized_cut_copper,
# stairs / slabs of all cut_copper variants (~24 blocks total)
```

**Excluded** (placed under MACHINE_FAMILY for tool/door logic):
`iron_door`, `iron_trapdoor`, `chain`, `iron_bars`.

Recombines:
- 4 `compressed_metal_fragment` → 1 iron_block / gold_block /
  copper_block / raw_*_block (player picks)
- 8 `compressed_metal_fragment` → 1 iron_ingot / gold_ingot /
  copper_ingot (alternative to ore-recombine path; expensive)

### #21 — PRECIOUS_BLOCKS *(drop: `precious_fragment`, tier: diamond, tool: pickaxe, mode: **replace**)*

Blocks:

```
diamond_block, emerald_block, netherite_block, lapis_block,
redstone_block,
amethyst_cluster, large_amethyst_bud, medium_amethyst_bud,
small_amethyst_bud, budding_amethyst,

# Devin addition: end-game blocks
beacon, conduit
```

**Note on amethyst_cluster:** vanilla drops 4 `amethyst_shard` with
iron+ pickaxe. With our system, drops 2 `precious_fragment`.
Recombine into amethyst_shard or amethyst_block via recipe.

**Note on `budding_amethyst`:** vanilla forbids breaking it for any
drop (block disappears). With our system: diamond-pick allows
breaking, but drops 2 `precious_fragment`. This is a *buff* (you can
now relocate budding_amethyst, where vanilla deletes it). Marking
this as a flag — alternative is to put `budding_amethyst` in
SPECIAL_UNBREAKABLE.

Recombines:
- 4 `precious_fragment` → 1 diamond_block / emerald_block /
  netherite_block / lapis_block / redstone_block
- 4 `precious_fragment` → 4 `amethyst_shard`
- 4 `precious_fragment` → 1 amethyst_block / amethyst_cluster
- 8 `precious_fragment` → 1 beacon
- 16 `precious_fragment` → 1 conduit

### #22 — MACHINE_FAMILY *(drop: `machine_scrap`, tier: iron, tool: pickaxe, mode: **replace**)*

Blocks:

```
hopper, dispenser, dropper, observer,
piston, sticky_piston,
beacon,                    # listed in #21 too — keep in #21
enchanting_table, anvil, chipped_anvil, damaged_anvil,
grindstone, brewing_stand, cauldron,
water_cauldron, lava_cauldron, powder_snow_cauldron,

rail, powered_rail, detector_rail, activator_rail,

# user "обходы":
iron_door, iron_trapdoor,
chain, lantern, soul_lantern, iron_bars,

# Devin additions (logically part of "machine"):
target, lightning_rod,
end_rod, redstone_lamp,
tripwire_hook, lever,
stone_pressure_plate, light_weighted_pressure_plate, heavy_weighted_pressure_plate,
stone_button,                  # already covered by stone family for stone_button? Resolve below
crafter,                       # 1.21 — N/A on 1.20.1
copper_grate, copper_bulb, copper_door, copper_trapdoor,    # 1.21 — N/A
```

**Conflict resolution:** `stone_button` and `stone_pressure_plate`
are wood-tier-pickaxe in vanilla. Move them to MACHINE_FAMILY
(consistent: anything that does redstone is "machine"). Otherwise,
add to SOFT_STONE. Devin's call: keep them in MACHINE_FAMILY for
narrative consistency.

**Note on `beacon`:** listed in both #21 PRECIOUS and #22 MACHINE.
Resolution: belongs in #21 (the construction is precious — needs a
pyramid of iron/gold/etc.) but a player breaking a placed beacon
gets `precious_fragment` (since the BLOCK is precious). Pick #21.

**Note on rails:** vanilla rails break with bare hand. Tightening to
iron-tier is the user's call (rails listed under "ЖЕЛЕЗНАЯ КИРКА").
Tradeoff: nether-portal start-game minecart play becomes iron-locked.

Recombines: many. 4 `machine_scrap` → 1 hopper / dispenser /
piston / iron_door / iron_trapdoor / chain / lantern / iron_bars /
etc. (one recipe per recoverable block).

### #23 — LIGHT_FAMILY *(drop: `light_fragment`, tier: hand, tool: any, mode: **replace**)*

Blocks:

```
torch, soul_torch, redstone_torch,
candle (uncolored), white_candle, orange_candle, magenta_candle,
light_blue_candle, yellow_candle, lime_candle, pink_candle,
gray_candle, light_gray_candle, cyan_candle, purple_candle,
blue_candle, brown_candle, green_candle, red_candle, black_candle,
sea_pickle,

# Devin additions:
glow_lichen      # listed in #0 too — keep in #0 (organic, not light)
```

**Note on torches:** vanilla torches break with bare hand and drop
themselves. Replace mode means torches → 2 `light_fragment` instead.
Once placed, you cannot pick them up. Recombine 4 `light_fragment` →
1 torch (50% loss). User accepts the gameplay change ("на твой
вкус").

**Conflict:** `glow_lichen` listed in #0 (ORGANIC) and #23 (LIGHT).
Resolution: keep in #0 (organic biome decoration; the "light" aspect
is incidental to the plant nature).

Recombines:
- 4 `light_fragment` → 4 `torch`
- 4 `light_fragment` → 4 `soul_torch`
- 4 `light_fragment` + 1 `redstone` → 4 `redstone_torch`
- 4 `light_fragment` → 1 `candle` (color via dye)
- 4 `light_fragment` → 4 `sea_pickle`

### #24 — SPECIAL_UNBREAKABLE *(no drop, no tier — never breakable)*

Blocks:

```
bedrock, end_portal_frame, end_portal,
end_gateway, command_block, chain_command_block, repeating_command_block,
barrier, structure_block, structure_void,
light_block, jigsaw,

# Devin additions:
spawner,                # vanilla pickaxe-mineable but drops no item
moving_piston,          # internal block
piston_head,            # internal block
fire, soul_fire,        # ephemeral
nether_portal,          # ephemeral
end_portal,             # special (already listed)
dragon_egg,             # vanilla "moves" on click; debatable
sculk_shrieker, sculk_sensor, sculk_catalyst, sculk_vein, sculk     # warden mechanics
# Actually sculk family should be HARD_STONE-tier minable for
# consistency. Move them out of UNBREAKABLE and into a new sub-set.
```

Explicit: any block in this category cannot be broken at any tier.
Mining gate hard-rejects.

---

## 5. Carve-outs and special cases (locked-in)

| Carve-out | Decision |
|---|---|
| **Crops** (wheat, carrots, potatoes, beetroot, sweet_berry_bush, pumpkin_stem, melon_stem) | Excluded from shard system. Vanilla drops only. (User Q1: "ты прав с едой ошибка".) |
| **Leaves** | Additive: `leaf_fragment × 2` + vanilla saplings/apples/sticks. (User Q2: "ты прав, листья и органику лучше не трогать".) |
| **All other organic plants** (grass, ferns, flowers, mushrooms, sugar_cane, kelp, cactus, etc.) | Additive (Devin extension): `organic_fiber × 2` + vanilla drops. Avoids per-plant carve-outs and preserves food / industry. |
| **Gravel** | Additive: `gravel_piece × 2` + vanilla flint (10%). Otherwise primitive_axe is uncraftable. |
| **Suspicious sand / suspicious gravel** | Excluded — vanilla brushing / archaeology preserved. |
| **Boats** | Removed from WOOD_FAMILY (entity, not block). Vanilla recipe stays. |
| **Silk-touch** | Bypasses GLM entirely. Vanilla loot only — gives the original block. (User Q6: "вариант б".) |
| **Workshop blocks** (furnace, smoker, blast_furnace, crafting_table, chest, barrel, etc.) | Replace mode. Breaking them gives shards, not the placed item. BTW-style "stations are precious". (User: "всё остальное на твой вкус, главное чтобы это не ломало мод и игру".) |
| **Spawner** | SPECIAL_UNBREAKABLE — vanilla allows breaking but drops nothing; we keep that. |
| **Budding amethyst** | (open, see #21 note) — likely SPECIAL_UNBREAKABLE. |
| **`correctToolForDrops` interaction** | GLM checks tool *type* per category. Wrong tool ⇒ no drop. Wrong tier ⇒ block doesn't break (gate). Tier sufficient + correct tool ⇒ shard drops. Silk-touch always wins. |

---

## 6. Block coverage audit

The following vanilla 1.20.1 blocks are **not in any of the user's
24 categories** but exist in-game. Devin's proposed assignment:

| Block | → Category | Notes |
|---|---|---|
| `bookshelf`, `chiseled_bookshelf` | #6 WOOD | wood + book ingredients; we still allow vanilla recipe |
| `composter` | #6 WOOD | wood-fence based |
| `bee_nest`, `beehive` | #6 WOOD | wood-block based |
| `pumpkin`, `melon`, `carved_pumpkin`, `jack_o_lantern` | #6 WOOD | axe-mineable in vanilla |
| `mushroom_block` (red/brown), `mushroom_stem` | #6 WOOD | giant-mushroom flesh |
| `azalea`, `flowering_azalea` | #6 WOOD | bush, axe-mineable |
| `bamboo_block`, `bamboo_mosaic*` | #6 WOOD | wood family |
| `mangrove_roots`, `muddy_mangrove_roots` | #2 DIRT (muddy) / #6 WOOD (dry) | split |
| `target` | #22 MACHINE | redstone block |
| `tripwire_hook`, `lever`, `*_button`, `*_pressure_plate` | #22 MACHINE | redstone |
| `lightning_rod` | #22 MACHINE | copper redstone |
| `redstone_lamp` | #22 MACHINE | redstone |
| `end_rod` | #22 MACHINE | end-tier light, but mechanical |
| `iron_bars` | #22 MACHINE | iron, blocks movement |
| `chain`, `lantern`, `soul_lantern` | #22 MACHINE | iron + light + decorative |
| `cake` | #0 ORGANIC | hand, vanilla drops nothing without silk |
| `coral`, `coral_block`, `coral_fan`, `dead_coral*`, `dead_coral_block`, `dead_coral_fan` | #18 CERAMIC (dead) / #0 ORGANIC (live, additive) | live coral keeps vanilla drop |
| `sponge`, `wet_sponge` | #18 CERAMIC | natural fired-clay analog |
| `hay_block`, `dried_kelp_block` | #0 ORGANIC (additive) | preserve vanilla edible item |
| `slime_block`, `honey_block`, `honeycomb_block` | #18 CERAMIC | sticky/fermented |
| `bell` | #22 MACHINE | iron mechanical |
| `lodestone` | #22 MACHINE | iron-base redstone |
| `dragon_egg` | #24 UNBREAKABLE | vanilla teleports on hit |
| `mob_spawner` | #24 UNBREAKABLE | nothing drops in vanilla |
| `skeleton_skull`, `wither_skeleton_skull`, `zombie_head`, `creeper_head`, `dragon_head`, `piglin_head`, `player_head` | #6 WOOD (axe) | mob trophy; axe-mineable in vanilla |
| `shulker_box` (16 colors) | #6 WOOD (with stone material… debate) | drop self with NBT in vanilla. **Carve-out: excluded** — keep vanilla self-drop with NBT to preserve player inventories. |
| `sculk`, `sculk_vein` | #8 HARD_STONE | stone-pickaxe required vanilla |
| `sculk_sensor`, `calibrated_sculk_sensor`, `sculk_shrieker`, `sculk_catalyst` | #22 MACHINE | redstone components |
| `frogspawn` | #0 ORGANIC | hand, additive |
| `pearlescent_froglight`, `verdant_froglight`, `ochre_froglight` | #15 HARD_DECORATIVE | iron-pick light |
| `mud_brick_*` (slab/stairs/wall) | #14 DECORATIVE | mud-brick family |
| All `*_slab`, `*_stairs`, `*_wall`, `*_door`, `*_trapdoor`, `*_button`, `*_pressure_plate`, `*_sign`, `*_hanging_sign`, `*_fence`, `*_fence_gate` derived from listed blocks | inherit category of base block | per-variant tagged in code |
| `nether_portal`, `end_portal`, `end_gateway`, `fire`, `soul_fire`, `lava`, `water` | #24 UNBREAKABLE | ephemeral / fluid |
| `trial_spawner`, `vault`, `crafter`, `copper_*` (1.21 blocks) | n/a | not in 1.20.1 |
| `note_block` | #6 WOOD | listed by user |
| `jukebox` | #6 WOOD | listed by user |

If a vanilla block is not in any category and not in the audit table
above, it falls through to SPECIAL_UNBREAKABLE (default-deny). The
implementation must scan all `Registry<Block>` entries at mod-load
and log any block that is unaccounted for, so we catch gaps.

---

## 7. Implementation plan

**Files added:**

1. **24 new items** in `ModItems.java`:
   `organic_fiber, leaf_fragment, dirt_chunk, sand_pile,
    gravel_piece, snow_chunk, wood_chip, stone_fragment,
    deepstone_fragment, ore_fragment, metal_fragment, rare_fragment,
    ancient_fragment, nether_fragment, brick_fragment,
    hard_brick_fragment, glass_shard, cloth_piece, ceramic_piece,
    concrete_dust, compressed_metal_fragment, precious_fragment,
    machine_scrap, light_fragment`.
   (`organic_fiber` replaces the existing `plant_fiber` from Phase
   1.1; old item is renamed via DeferredRegister, references in
   recipes / GLMs migrated.)
2. **24 GLM rules** at `data/glebthanwolves/loot_modifiers/<name>.json`.
   One per category, pointing at a custom modifier
   `glebthanwolves:block_shard` with parameters:
   - `mode` ∈ {replace, additive, excluded}
   - `tool_type` ∈ {any, axe, pickaxe, shovel, shears, hand}
   - `shard_id` (e.g. `glebthanwolves:wood_chip`)
   - `shard_count` (default 2)
   - `target_tag` (e.g. `glebthanwolves:has_wood_chip`)
3. **24 block tags** at `data/glebthanwolves/tags/blocks/has_<name>_shard.json`.
   Each lists the blocks in that category (per §4).
4. **6 mining-gate tags** at `data/glebthanwolves/tags/blocks/breakable_by/*.json`,
   referencing the appropriate shard tags per tier:
   - `hand.json` → has_organic_fiber, has_leaf_fragment, has_dirt_chunk,
     has_sand_pile, has_gravel_piece, has_snow_chunk, has_cloth_piece,
     has_light_fragment
   - `primitive.json` (= wood-tier) → all `hand` includes + has_wood_chip,
     has_stone_fragment, has_ore_fragment, has_brick_fragment,
     has_glass_shard, has_ceramic_piece
   - `stone.json` → all `primitive` includes + has_deepstone_fragment,
     has_metal_fragment, has_nether_fragment, has_concrete_dust
   - `iron.json` → all `stone` includes + has_rare_fragment,
     has_hard_brick_fragment, has_compressed_metal_fragment,
     has_machine_scrap
   - `diamond.json` → all `iron` includes + has_ancient_fragment,
     has_precious_fragment
   - `netherite.json` → just includes `diamond` (empty otherwise; tier
     reserved for future)
5. **Custom GLM Java class:** `data/loot_modifiers/BlockShardModifier.java`,
   registered via `RegisterEvent` for `Registries.GLOBAL_LOOT_MODIFIER_SERIALIZERS`.
   Implements:
   - silk-touch bypass
   - tool-type check
   - additive vs replace vs excluded mode
6. **Recipes** at `data/glebthanwolves/recipes/<target>_from_<shard>.json`.
   Roughly ~150 recipes (every category has 4–10 reassemble targets).
7. **Lang strings** for 24 items in `en_us.json` and `ru_ru.json`.
8. **Models** for 24 items at `assets/glebthanwolves/models/item/<name>.json`,
   each pointing at a placeholder texture (or vanilla texture analog
   where one fits — e.g. `wood_chip` → `minecraft:item/stick`-like;
   `stone_fragment` → `minecraft:item/cobblestone` reduced; etc.).
   Per user "потом изменим", we keep the bar low here.
9. **Update `MiningGate.java`** if needed for tool-type checks. (Most
   tool-type logic lives in the GLM, not the gate. Gate still checks
   tier only.)

**Files removed / renamed:**

- `plant_fiber` item: renamed to `organic_fiber`. Migration path:
  add a one-shot crafting recipe `1 plant_fiber → 1 organic_fiber`
  for any old-world stacks; or use `MissingMappings` + alias to
  redirect the registry name. (Recommended: alias, no recipe needed.)

**Migration order (one PR, but ordered commits for review):**

1. Commit 1: 24 new items + lang + models.
2. Commit 2: 24 block tags listing the blocks per category.
3. Commit 3: Custom GLM Java class + 24 GLM JSONs.
4. Commit 4: Mining gate `breakable_by/*.json` tags updated to
   reference the shard tags.
5. Commit 5: All recombine recipes.
6. Commit 6: Doc updates (CHANGELOG / HISTORY / ROADMAP / this file).
7. Commit 7: Audit script — at mod-load, iterate all
   `ForgeRegistries.BLOCKS` and warn if any non-air block isn't tagged.

---

## 8. Open issues for user decision (RESOLVED — see §0)

All 7 open issues below were resolved by user in chat on 2025-05-07.
The resolutions are detailed in §0; this section is preserved for
historical context only.


1. **Fungibility of metal_fragment / rare_fragment / ore_fragment.**
   Should `metal_fragment` be one item (mine lapis, get fragments,
   craft raw_iron) or split into `iron_fragment / lapis_fragment /
   redstone_fragment`? Devin recommends one (matches user's "вариант
   А" preference for stone family).
2. **Budding amethyst.** Vanilla deletes it on break. Should we keep
   that (SPECIAL_UNBREAKABLE) or allow diamond-pick to harvest it?
3. **Shulker box.** Player-stored items are inside its NBT. If we
   replace drops with shards, shulker contents are lost. Devin
   recommends excluded — vanilla self-drop with NBT preserved.
4. **Spawner.** Vanilla pickaxe-mineable, drops nothing. Devin
   recommends SPECIAL_UNBREAKABLE.
5. **Tool type for SAND/DIRT/SNOW.** User's #2/#3/#5 say "РУКА" but
   vanilla shovel-only drops. Devin proposal: tool type = `any`
   (hand or shovel both work). User's intent matches.
6. **Recombine cost calibration.** Default "крутая потеря" rule says
   4 shards → 1 block (50% loss). For BTW-style stations
   (crafting_table, chest, furnace), that's effectively the same as
   building one from scratch (no extra penalty). Should stations
   cost more (e.g. 8 shards) to make station-loss really painful?
7. **Recipe advancements.** Still not generated (open issue from
   Phase 2.2 onward). When this PR adds ~150 recipes, the recipe
   book will be very crowded with locked entries. Should we generate
   advancements as part of this PR (datagen RecipeProvider) or defer
   to a separate cleanup PR?

---

## 9. Estimated PR size

- ~24 new Java item registrations
- ~1 new Java class (`BlockShardModifier.java`, ~300 lines)
- ~24 GLM JSON files
- ~24 block tag JSON files (with hundreds of block entries
  collectively)
- ~6 mining-gate tag JSON files (updated)
- ~150 recipe JSON files
- ~24 model JSON files
- ~48 lang entries (en + ru)
- ~600 lines of doc updates (CHANGELOG / HISTORY / ROADMAP / this
  file finalized)

Total: probably **~250 files changed / added, ~5000 LOC**. The
biggest single GTW change yet, by a factor of 5–10.
