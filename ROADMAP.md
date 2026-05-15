# Roadmap — Phase 2.0 and beyond

This document captures the long-term plan for the mod. Phase 1.x finished a
*hardcore tweaks* layer (~30 mechanics) on top of vanilla. Phase 2.x is the
real ambition: **redesigning Minecraft's progression**.

The user's stated goal:

> "В будущем я хочу заменить почти все предметы и крафты, чтобы было супер
> сложно, можно сказать перестроить майнкрафт. Но в майнкрафте слишком много
> механик — найти предметы в сундуках, выбить с мобов, купить у жителя — всё
> это предусмотреть почти невозможно."
>
> "В будущем по плану после физических усложнений переделать крафты, сделать
> свою цепочку развития, а потом переходить к другим мирам."

So: hardcore tweaks (done) → custom progression (Phase 2.x) → other dimensions
overhaul (Phase 3.x).

---

## Architectural principles (from the Phase 2 design discussion)

These are the seven principles that make a redesign tractable. They were
agreed in a long architectural discussion (see `HISTORY.md` → "Architectural
discussion 2025-05-04").

### 1. Replace, don't delete

**Don't try to delete vanilla items.** The game will generate them anyway
(structures, chests, mob drops, fishing). Instead, **make them useless** by
replacing every recipe that uses them with one that uses your own item.
Vanilla iron ingot becomes a souvenir.

### 2. Layer-based progression via tags

Define a tag tier per item class:
- `gtw:tier/0` — stone age
- `gtw:tier/1` — copper age
- `gtw:tier/2` — iron age
- `gtw:tier/3` — steel age
- `gtw:tier/4` — endgame

In code, **always check tags, never hardcode item lists**. Adding a new ore
just means tagging it; everything else propagates automatically.

### 3. Datapack is the primary tool

90% of redesign work can happen in JSON, no Java:
- `data/minecraft/loot_tables/entities/zombie.json` — override mob drops
- `data/minecraft/loot_tables/chests/simple_dungeon.json` — override chest loot
- `data/minecraft/recipes/iron_pickaxe.json` (with empty / 0-count result) —
  disable a vanilla recipe
- `data/glebthanwolves/recipes/...` — add ours

Datapack ships inside the .jar at `src/main/resources/data/`. Forge
auto-applies it.

### 4. Forge events for dynamic surfaces

What datapack can't do, Forge events can:
- `VillagerTradesEvent` / `WandererTradesEvent` — control villager trades
  fully (clear, replace, restrict).
- `EntityItemPickupEvent` — filter what a player can pick up (the bottleneck
  for "can't have item X").
- `PlayerEvent.ItemCraftedEvent` — post-craft hook.
- `EntityJoinLevelEvent` — modify spawning entities.
- `AnvilUpdateEvent` — anvil interactions.
- `BrewingRecipeRegistry` — brewing.
- `ServerStartingEvent` — strip recipes from RecipeManager (aggressive).

### 5. Single chokepoint for banned items

One static `Set<Item> BANNED_ITEMS` plus one `EntityItemPickupEvent` hook:

```java
@SubscribeEvent
public void onPickup(EntityItemPickupEvent event) {
    if (BANNED_ITEMS.contains(event.getItem().getItem().getItem())) {
        event.setCanceled(true);  // item stays on ground, despawns naturally
    }
}
```

This catches **every source** of an item: chest, mob drop, fishing, trade
that drops items, suspicious sand, etc. One hook = total control.

### 6. Transmutation instead of deletion

Better than blocking vanilla items — let them be **convertible** at a loss:
- 1 vanilla iron ingot in crafting grid → 1 "iron scrap" (worth 1/4 of a
  proper GTW iron ingot).
- Player can use found loot, just inefficiently.
- Removes the frustration of "found a great chest → all useless".

### 7. Top-down design of the craft tree

Don't build bottom-up ("what should break stone?"). Build top-down:

1. End goal: diamond sword
2. What it needs: diamond + handle
3. What diamond needs: ore + iron pickaxe + ...
4. What iron pickaxe needs: iron ingot + ...
5. What iron ingot needs: ore + bloomery + charcoal + hammer + ...

The tree branches naturally. Each tier is a separate phase, separately
shippable as a PR.

---

## Phase 2 plan

### Phase 2.0 — Armor gate first ✅ done

- Disabled vanilla iron/gold/diamond armor crafting via datapack recipe
  overrides.
- Only leather and chainmail armor are allowed to be worn.
- Found/dropped strong armor is rejected on equip via
  `LivingEquipmentChangeEvent` and `PlayerInteractEvent.RightClickItem`.
- Open question: chainmail as early loot armor or a custom craft path
  later — defer.

### Phase 2.1 — Tool craft gate ✅ done

- 30 vanilla tool recipes off (5 tiers × 5 tool types: wooden / stone / iron
  / golden / diamond × pickaxe / axe / shovel / hoe / sword) plus 5
  netherite smithing recipes.
- 4 special items off: bow, crossbow, flint and steel, shield.
- Mod tools (`glebthanwolves:primitive_axe` and any future `glebthanwolves:*`
  items) untouched.
- Found vanilla tools (chest loot, mob drops, structure loot) still work.
- Implementation: `forge:false` recipe override JSONs, identical pattern to
  Phase 2.0.

### Phase 2.2.x — Crude wooden + iron tools ✅ done

Finishes the GTW tool set up through iron, using the same "Crude" pattern
as Phase 2.2: vanilla baselines, ~50% durability, ~75% mining speed,
vanilla textures reused. 10 new items (5 wood + 5 iron) plus two new
tiers (`GTW_WOODEN`, `GTW_IRON`) in `GtwTiers.java`. Recipes use planks
or iron_ingot + stick + plant_cordage (cordage replaces one stick;
sword has no stick at all). Models reuse vanilla textures
(`minecraft:item/wooden_*`, `minecraft:item/iron_*`).

### Phase 2.2 — Crude stone tools ✅ done

First GTW tier of real (mineable) tools. 5 items registered
(`stone_pickaxe`, `_axe`, `_sword`, `_shovel`, `_hoe`) with custom Tier
`GtwTiers.GTW_STONE`: durability 70, mining speed 3.0, +1.0 attack damage,
mining level 1, enchantability 5, repair = cobblestone. Damage / attack
speed numbers identical to vanilla `Items.STONE_*`. Recipes use
`plant_cordage` as a binding ingredient, gating the tier through the
bushcraft chain. Models reuse vanilla textures.

**Open issues (must be resolved before Phase 2.3):**

1. **First-cobblestone problem.** With Phase 2.1 disabling vanilla
   wooden / stone pickaxe and Phase 2.2's own `stone_pickaxe` requiring
   cobblestone, there is no in-mod way to break a single stone block.
   The player has to find a vanilla pickaxe in chest / mob loot. Possible
   resolutions (one to be picked):
   - Add a `primitive_pickaxe` (flint + cordage + stick, durability ~8,
     mining level 0 — can break stone but nothing harder). Tiny PR.
   - Give `primitive_axe` the `PICKAXE_DIG` tool action so it can mine
     stone. Conceptually muddier ("axe = pickaxe"), but zero new files.
   - Add a small chance for cobble drops to leaves / gravel via GLM and
     accept the awkwardness.
   - Treat "find a vanilla pickaxe in loot" as the intended path and
     boost structure loot tables in Phase 2.7.
2. **No recipe advancements.** New `data/glebthanwolves/recipes/stone_*`
   files have no matching `data/glebthanwolves/advancements/recipes/*`
   files, so the recipes will not auto-unlock in the player's recipe
   book. Should be generated via `RecipeProvider` (datagen) before the
   next playable jar.
3. **Visual identity.** Models reuse vanilla `minecraft:item/stone_*`
   textures, so GTW stone tools look identical to vanilla in inventory.
   Only the lang label (`Crude Stone *`) marks them. Acceptable for now;
   may want unique textures later.

### Phase 2.3a — Mining gate scaffold (default-deny, EMPTY whitelist) ✅ done

Inverts the vanilla "all blocks breakable, only tier-gated" rule. Now
**all blocks are unbreakable** by default, and each tier has a datapack
whitelist (`glebthanwolves:breakable_by/<tier>`) of allowed blocks. Higher
tiers include lower tiers via tag-include.

The PR ships the gate **with empty whitelists** by user decision — no
block is allowed for any tier yet. Spawn is intentionally a soft-lock
until follow-up PRs unlock blocks one at a time.

Implementation: `events/MiningGate.java` (Forge `PlayerEvent.BreakSpeed`
+ `BlockEvent.BreakEvent`), 6 tag files, registered handler.

### Phase 2.2.y — Crude diamond tools ✅ done

Top of the GTW tier ladder. `GTW_DIAMOND` mirrors `Tiers.DIAMOND` (level
3, +3.0 dmg) but with 50% durability (780 vs 1561) and 75% mining speed
(6.0 vs 8.0). Repair = `diamond`. 5 new items: `diamond_pickaxe / _axe
/ _sword / _shovel / _hoe`, vanilla baseline HUD attack stats, vanilla
textures reused. Recipes use `minecraft:diamond` + stick + cordage
(sword: cordage, no stick). Same caveats as Phase 2.2.x — recipe
advancements aren't generated, models reuse vanilla textures, mining
gate is still empty so the tools can be crafted but cannot break any
block until Phase 2.3b lands.

### Phase 2.3b — Block shards + 24-category mining logic (planned, in-design)

User's final design pivot (replaces the earlier "variant A, universal
stone_shard" idea): **24 distinct shard items**, one per block family.
The full design is in `MINING_DESIGN.md` (also linked from `HISTORY.md`).
Summary of categories:

| # | Category | Drop | Tier (mine) |
|---|---|---|---|
| 0  | ORGANIC_SOFT      | `organic_fiber`              | hand    |
| 1  | LEAF_FAMILY       | `leaf_fragment` (additive)   | hand    |
| 2  | DIRT_FAMILY       | `dirt_chunk`                 | hand    |
| 3  | SAND_FAMILY       | `sand_pile`                  | hand    |
| 4  | GRAVEL_FAMILY     | `gravel_piece` (additive)    | hand    |
| 5  | SNOW_FAMILY       | `snow_chunk`                 | hand    |
| 6  | WOOD_FAMILY       | `wood_chip`                  | wood    |
| 7  | SOFT_STONE        | `stone_fragment`             | wood    |
| 8  | HARD_STONE        | `deepstone_fragment`         | stone   |
| 9  | BASIC_ORE         | `ore_fragment`               | wood    |
| 10 | METAL_ORE         | `metal_fragment`             | stone   |
| 11 | RARE_ORE          | `rare_fragment`              | iron    |
| 12 | ANCIENT_MATERIAL  | `ancient_fragment`           | diamond |
| 13 | NETHER_STONE      | `nether_fragment`            | stone   |
| 14 | DECORATIVE_BLOCKS | `brick_fragment`             | wood    |
| 15 | HARD_DECORATIVE   | `hard_brick_fragment`        | iron    |
| 16 | GLASS_FAMILY      | `glass_shard`                | wood    |
| 17 | WOOL_FAMILY       | `cloth_piece`                | hand    |
| 18 | CERAMIC_FAMILY    | `ceramic_piece`              | wood    |
| 19 | CONCRETE_FAMILY   | `concrete_dust`              | stone   |
| 20 | METAL_BLOCKS      | `compressed_metal_fragment`  | iron    |
| 21 | PRECIOUS_BLOCKS   | `precious_fragment`          | diamond |
| 22 | MACHINE_FAMILY    | `machine_scrap`              | iron    |
| 23 | LIGHT_FAMILY      | `light_fragment`             | hand    |
| 24 | SPECIAL_UNBREAKABLE | (no drop, can't be mined) | n/a     |

**Carve-outs from the "everything → shard" rule** (locked-in design
decisions, see `MINING_DESIGN.md` for rationale):

- **Crops** (`wheat`, `carrots`, `potatoes`, `beetroot`,
  `sweet_berry_bush`) are **not** in the shard system — vanilla drops
  remain so food chains still work.
- **Leaves** drop `leaf_fragment` **additively** alongside vanilla
  saplings / apples / sticks, otherwise sapling regen breaks and the
  world soft-locks.
- **Gravel** drops `gravel_piece` **additively** with vanilla flint
  (10%), otherwise `primitive_axe` becomes uncraftable on spawn.
- **Suspicious sand / suspicious gravel** are excluded from the shard
  system; vanilla brushing / archaeology stays as-is.
- **Boats** are entities, not blocks — they don't go through the
  mining gate. (Listed in WOOD_FAMILY as a craft-side concern only.)
- **Silk-touch** bypasses the shard GLM and drops the original block
  (Q6: "вариант б — silk-touch обходит shard-механику").
- **Furnace / chest / crafting_table / stations** drop their family
  shard on break, **not the placed item** (BTW-style commitment —
  once placed, you cannot relocate them). This is the user's
  intentional design from the SOFT_STONE / WOOD_FAMILY listings.

**Ore recombine recipes** (per user: ores also drop fragments, then
combine back):

- 4 `ore_fragment` → 1 `coal` (or 1 `raw_copper`) — player picks recipe.
- 4 `metal_fragment` → 1 `raw_iron` (or 4 `lapis_lazuli` / 4 `redstone`).
- 4 `rare_fragment` → 1 `raw_gold` / 1 `emerald` / 1 `diamond` / 1
  `quartz`.
- 4 `ancient_fragment` → 1 `ancient_debris` (block, not netherite).

**Implementation skeleton (planned for the PR):**

1. 24 new items in `ModItems.java` (one per category).
2. 24 GLMs (one per category) in `data/glebthanwolves/loot_modifiers/`.
3. 24 block tags (`glebthanwolves:has_<name>_shard`) listing every block
   that drops that fragment.
4. Mining gate whitelists (`breakable_by/<tier>`) updated to reference
   the 24 category tags via `forge:replace=false` includes.
5. Recombine recipes (one per re-craftable target — many).
6. `MINING_DESIGN.md` design document with the full block list (~750
   blocks) annotated by category, including the not-yet-categorized
   blocks (bookshelf, composter, beehive, mob heads, sculk family,
   amethyst clusters, copper variants, slabs / stairs / walls of every
   stone family, banners, shulker boxes, etc.).

### Phase 2.5+ — Targeted shards (current plan, replaces failed 2.3b)

After the rollback of 2.3b (151-recipe 18-category disaster), the agreed
plan is **one shard per material, one small PR each, only for blocks/ores
that are needed in vanilla crafts**. Each shard ships independently, the
user tests, signs off, then the next one is built.

**Final agreed shard list (in implementation order):**

| Order | Shard | Ratio | Source blocks | Status |
|---|---|---|---|---|
| 1 | `cobblestone_fragment` | 2 → 1 cobble | `minecraft:stone` only | ✅ Phase 2.5 done |
| 2 | `iron_fragment` | 4 → 1 raw_iron | `iron_ore`, `deepslate_iron_ore` | planned |
| 3 | `gold_fragment` | 4 → 1 raw_gold | `gold_ore`, `deepslate_gold_ore`, `nether_gold_ore` | planned |
| 4 | `diamond_fragment` | 4 → 1 diamond | `diamond_ore`, `deepslate_diamond_ore` | planned |
| 5 | `copper_fragment` | 4 → 1 raw_copper | `copper_ore`, `deepslate_copper_ore` | planned |
| 6 | `quartz_fragment` | 4 → 1 nether_quartz | `nether_quartz_ore` | planned |
| 7 | `coal_fragment` | 2 → 1 coal | `coal_ore`, `deepslate_coal_ore` | planned |

**Skipped (per user):** netherite tools, lapis_fragment, emerald_fragment.

**Implementation pattern (lessons from failed 2.3b):**

1. **Override the vanilla loot table** at `data/minecraft/loot_tables/blocks/<ore>.json`
   — single file, replaces the entire vanilla drop pool. Silk-touch path
   preserved when it makes sense (e.g. `minecraft:stone` block under
   silk touch still drops `minecraft:stone`).
2. **One shapeless recipe** at `data/glebthanwolves/recipes/<material>_from_fragments.json`
   — N fragments → 1 vanilla material item. Player crafts in 2×2 inventory
   grid, no table needed.
3. **No mining-gate, no whitelist, no default-deny.** Vanilla tier gating
   (`needs_*_tool`) already handles "wrong pickaxe → no drop"; the shard
   override only changes WHAT drops when the pickaxe IS correct.
4. **Fortune behavior:** preserve vanilla — ore shards inherit the same
   `apply_bonus(fortune, ore_drops)` function the original loot table used.
   `minecraft:stone` → `cobblestone_fragment` does NOT get Fortune, since
   vanilla cobble from stone also doesn't.
5. **Chest loot untouched.** Player can still find vanilla ingots/gems in
   chests (mineshaft, dungeon, etc.). May revisit later if it undermines
   the shard mechanic too much.

### Phase 2.3c — Found tools weaker (planned, deferred)

User said in chat (Q7): "пока ничего не делаем, а потом заменим спавн на
наши инструменты" — i.e. found-vanilla-tool weakening is **deferred** in
favor of replacing the spawn (structure loot tables in Phase 2.7) so that
vanilla tools don't appear at all. The mechanics below are kept as a
fallback in case structure-loot replacement turns out to leak vanilla
tools through some path.

### Phase 2.3c — Found tools weaker (planned, deferred)

User said in chat (Q7): "пока ничего не делаем, а потом заменим спавн на
наши инструменты" — i.e. found-vanilla-tool weakening is **deferred** in
favor of replacing the spawn (structure loot tables in Phase 2.7) so that
vanilla tools don't appear at all. The mechanics below are kept as a
fallback in case structure-loot replacement turns out to leak vanilla
tools through some path.

**Fallback mechanics:**

1. **NBT-tag at pickup.** The first time a player picks up a vanilla tool,
   we set `GTWFoundTool=true` on the stack via `EntityItemPickupEvent` /
   `PlayerEvent.ItemPickupEvent`. This tag travels with the stack across
   chests, hoppers, drops, etc.
2. **Damage scaling.** `LivingHurtEvent`: if the attacker's main hand has
   `GTWFoundTool=true`, multiply `event.getAmount()` by 0.5. Tool deals
   half its base damage.
3. **Break-speed scaling.** `PlayerEvent.BreakSpeed`: if held item has
   `GTWFoundTool=true`, multiply `event.getNewSpeed()` by 0.5. (Stacks
   with the Mining Gate from Phase 2.3a, which sets speed to 0 if the
   tool's tier disallows the block.)
4. **Faster wear.** On each tool use that consumes durability, also call
   `stack.hurtAndBreak(1, ...)` an extra time → tool breaks 2× faster.
5. **HUD hint.** Optional: lore line on the item like `"§7Found tool — half
   stats, half durability"` so the player knows.

**Sources to study before implementation:**
- Tinkers' Construct (https://github.com/SlimeKnights/TinkersConstruct, MIT)
  — has a "broken tool" concept with reduced stats.
- Forge's `IItemHandler` and `EntityItemPickupEvent` examples in the Forge
  docs.

A separate report will be filed before any external code is referenced; see
the rules in `HISTORY.md` → "External code policy".

### Phase 2.4 — Tags, banned-items chokepoint, no-trade scaffolding (1 PR)

- Define `gtw:tier/0..4` tag files.
- Create `BannedItems` static class + `EntityItemPickupEvent` hook (initially
  empty list).
- `VillagerTradesEvent` + `WandererTradesEvent`: clear ALL vanilla trades.
- Result: trading completely broken (the user already wanted this in Phase
  1.7, but now it's enforced via the redesign infrastructure).

### Phase 2.5 — Alternative iron (3-4 PRs)

- Add `gtw:raw_iron`, `gtw:iron_ingot`, plus tools that use them.
- All vanilla recipes consuming iron → consume `gtw:iron_ingot` instead
  (datapack overrides).
- Multi-step iron path:
  - Mine GTW raw iron with stone or copper pickaxe.
  - Crush raw iron with a "bloomery hammer" block (new) into iron powder.
  - Smelt iron powder in a "bloomery" block (new) over charcoal — slow.
  - Output: GTW iron ingot.
- Vanilla iron ingot → tagged `gtw:legacy/iron`. In a "legacy converter"
  recipe, 1 vanilla iron → 1 `gtw:iron_scrap` (1/4 of a real ingot).
- Result: iron exists, but the vanilla path is dead. Found loot is
  partially useful.

### Phase 2.6 — Same treatment for every tier (one PR per tier)

- Copper (existing vanilla, just tag and gate).
- Gold (mostly cosmetic; convert vanilla recipes that use gold → GTW gold).
- Diamond.
- Netherite.

Each tier follows the same pattern: new ore, new processing chain, datapack
recipe overrides, legacy-conversion path.

### Phase 2.7 — Structures and loot tables

- Override every vanilla structure loot table to drop GTW versions.
- Add new structures with rare ingredients:
  - Ruined towers — high-tier iron components.
  - Witch huts — alchemy reagents.
  - Buried bunkers — mid-tier blueprints.

### Phase 2.8 — Selective trading

- Re-enable trades, but heavily restricted:
  - Each profession sells exactly 1–2 items.
  - Prices scale with progression difficulty.
  - Wandering Trader sells only flavor items (banners, flowers, dyes).

### Phase 2.9 — Survival systems on top

- Thirst (Tough as Nails-style, but our own implementation).
- Body temperature (we already have desert heat / snow cold; extend to a
  full thermal model).
- Disease (build on the existing zombie infection; add bleeding, fractures,
  fatigue).

---

## Phase 3 plan (sketch only)

### Phase 3.0 — Nether overhaul

- Nether becomes a proper *second* progression layer, not a shortcut.
- New ores, blocks, crafting blocks unique to the dimension.
- Existing nether structures (fortress, bastion) get heavy loot redesign.

### Phase 3.1 — End overhaul

- Same treatment for the End. End becomes *third* progression layer.
- Endgame items (elytra-equivalent, beacon-equivalent) come from a long
  end-only chain.

---

## Status of the original 25-mechanic brainstorm (2025-05-04)

The user picked these from a list of 44 brainstormed ideas. **All 25 are
implemented as of commit `1aeff4c`.** Numbering matches the original
brainstorm (which is reproduced in `HISTORY.md`).

| # | Status | Mechanic |
|---|---|---|
| 1 | done | Sneak ×0.5 |
| 2 | done | Snow / powder snow -25% |
| 3 | done | Ice slippery ×1.5 |
| 4 | done | Encumbrance (≥10 stacks → Slow + Mining Fatigue) |
| 6 | done | Swim -30% |
| 7 | done | Climb (ladder/vine) ×0.7 |
| 9 | done | Silent creepers (15%) |
| 10 | done | Witches everywhere (every 30 min, 50% chance) |
| 12 | done | Husk replaces zombie (10% any biome) |
| 13 | done | Ghasts shoot extra fireball (60-tick interval, 60% chance) |
| 14 | done | Headless creeper (20%, +radius) |
| 15 | done | Lightning ×3 in storms |
| 17 | done | Desert heat at noon (1 heart / 8 sec, no helmet, sky visible) |
| 18 | done | Cold biomes (1 heart / 30 sec, no chest armor, sky visible) |
| 19 | done | Lava ×1.5 + 10 sec on fire after exit |
| 20 | done | Meteors at night |
| 21 | removed | Rain blindness (user clarified they didn't want this) |
| 23 | done | Zombie infection 15% Hunger II 5 min |
| 24 | done | Death fever 5 min Weakness + Mining Fatigue |
| 27 | done | Sleep deprivation after 2 days |
| 36 | done | 50% XP keep on death |
| 37 | done | XP orbs 30 sec lifetime |
| 39 | done | Swamp slow in water |
| 40 | done | Cactus ×2 damage |
| 41 | done | Sweet berries ×3 damage |

### Brainstorm ideas not picked (still available for future selection)

Numbers from the 44-item list. Ordered by category. Difficulty (★ ★★ ★★★)
indicated per item.

**Movement / navigation:**
- 5. ★ Jump -10% (negative JUMP_STRENGTH modifier)

**Predators:**
- 8. ★ Endermen aggro at 16 blocks without eye contact
- 11. ★ Drowned spawn in any water at night

**Environment:**
- 16. ★★ Desert sandstorms (15-min interval, Blindness + Slowness for 2 min)

**Physiology:**
- 22. ★ Bleeding (Wither I 60 sec, 20% chance on sword/axe/arrow hit)
- 25. ★ Fall shock (Slowness II 3 sec on >5-block fall)
- 26. ★★ Mining fatigue from continuous mining (>100 blocks → Mining Fatigue)

**Perception:**
- 28. ★ Darker darkness (light-0 → micro-flicker Blindness)
- 29. ★★ Underground compass goes random (y < 50)
- 30. ★★ HUD hidden at low HP (HP < 2 hearts → hide hotbar/exp/food bars)

**Equipment:**
- 31. ★ Tools 1.5× faster break (durability ×0.7)
- 32. ★ Armor 1.5× faster break (durability ×0.7)
- 33. ★ Dropped tool durability degrades on hard impact
- 34. ★ Items on the ground despawn after 90 sec (instead of 5 min)

**World:**
- 35. ★ Bed only sets spawn 70% of the time
- 38. ★★ Bedrock layer pushed up to y=5 (rarer diamonds)

**Atmospheric:**
- 42. ★★ Cave ambient sounds more frequent
- 43. ★★ Glowing eyes in dark bushes (50% real mob, 50% none)
- 44. ★ Mobs moan more often when nearby

If the user wants any of these added later, the implementation pattern in
`ARCHITECTURE.md` ("How to add a new mechanic") is the recipe.

---

## Open questions for the user

These came up during Phase 1 implementation but were never resolved. New
agents picking up this work should ask the user before guessing:

1. **Witch spawn frequency** — currently 30 min / 50% chance per player. Maybe
   too sparse; maybe too dense. User to confirm after playtest.
2. **Meteor lethality** — currently power 3 explosion, 50–110 blocks away.
   Could be made player-targeting if too forgiving.
3. **Sleep deprivation threshold** — currently 2 vanilla days (48000 ticks).
   May need adjustment depending on how often the user actually sleeps in
   their playstyle.
4. **Encumbrance threshold of 10** — could go lower (5? 7?) for harsher feel.
5. **Trade ban side effects** — currently leads, name tags, and zombie
   villager curing all blocked because they share the right-click hook. User
   asked to flag this; awaiting decision on whether to whitelist any.

---

## Dependency-free design

The mod has **zero hard dependencies** on other mods. It's pure Forge + MC.
This is intentional — for Phase 2 redesign work, soft dependencies on JEI
and AppleSkin will be added (display layer only), but the mechanics layer
should never require another mod to function.
