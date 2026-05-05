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

### Phase 2.0 — Armor gate first

- Disable vanilla iron/gold/diamond armor crafting via datapack recipe
  overrides.
- Allow only leather and chainmail armor to be worn.
- Block found/dropped strong armor from being equipped through right-click or
  inventory/equipment changes.
- Next: decide whether chainmail stays as early loot armor or becomes a custom
  craft path.

### Phase 2.1 — Tags, banned-items chokepoint, no-trade scaffolding (1 PR)

- Define `gtw:tier/0..4` tag files.
- Create `BannedItems` static class + `EntityItemPickupEvent` hook (initially
  empty list).
- `VillagerTradesEvent` + `WandererTradesEvent`: clear ALL vanilla trades.
- Result: trading completely broken (the user already wanted this in Phase
  1.7, but now it's enforced via the redesign infrastructure).

### Phase 2.2 — Alternative iron (3-4 PRs)

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

### Phase 2.3 — Same treatment for every tier (one PR per tier)

- Copper (existing vanilla, just tag and gate).
- Gold (mostly cosmetic; convert vanilla recipes that use gold → GTW gold).
- Diamond.
- Netherite.

Each tier follows the same pattern: new ore, new processing chain, datapack
recipe overrides, legacy-conversion path.

### Phase 2.4 — Structures and loot tables

- Override every vanilla structure loot table to drop GTW versions.
- Add new structures with rare ingredients:
  - Ruined towers — high-tier iron components.
  - Witch huts — alchemy reagents.
  - Buried bunkers — mid-tier blueprints.

### Phase 2.5 — Selective trading

- Re-enable trades, but heavily restricted:
  - Each profession sells exactly 1–2 items.
  - Prices scale with progression difficulty.
  - Wandering Trader sells only flavor items (banners, flowers, dyes).

### Phase 2.6 — Survival systems on top

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
