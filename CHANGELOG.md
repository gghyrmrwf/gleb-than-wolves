# Gleb Than Wolves — Changelog

**Minecraft 1.20.1 / Forge 47.2.0 / Java 17**
Mod ID: `glebthanwolves` · Package: `com.gghyrmrwf.glebthanwolves`
Repo: https://github.com/gghyrmrwf/gleb-than-wolves
Active PR: https://github.com/gghyrmrwf/gleb-than-wolves/pull/2
Active branch: `devin/1777925074-phase-1-1-bushcraft`

This changelog covers everything from Phase 0 (empty mod scaffold) through Phase
1.14 plus the post-1.14 fix pass. All numeric parameters live as named constants
at the top of the relevant Java file — search by the parameter name in the file
listed under each phase.

## Testing / verification status

- Phase 0 was verified in the dev client: the mod loaded as
  `Gleb Than Wolves 1.0.0`.
- Phase 1.1–1.4 were verified by building `glebthanwolves-1.0.0.jar` and by
  the manual checklist in PR #2.
- Phase 1.7 golem hostility was verified in the dev client: a spawned golem
  targeted and killed the survival player.
- Phase 1.10–1.14 were packaged into a jar for user playtesting. The user then
  reported issues with encumbrance, wrong brainstorm items, and desert heat;
  those were fixed in commit `1aeff4c`.
- Phase 2.0 armor gate was build-verified locally; needs in-game testing for
  recipe removal and both equip paths.
- Current recommended local check before sending any jar: `./gradlew build`.

---

## Phase 0 — Mod scaffold

**Files added:**
- `gradle.properties`, `build.gradle`, `settings.gradle`
- `src/main/resources/META-INF/mods.toml`
- `src/main/java/com/gghyrmrwf/glebthanwolves/GlebThanWolves.java`
- `src/main/resources/pack.mcmeta`
- `LICENSE` (MIT)
- `README.md`

**What it does:** Loads as `Gleb Than Wolves 1.0.0` in the F3 mod list. No
gameplay changes.

**Verified:** Dev client `runClient` task lists the mod in the Mods screen.

---

## Phase 1.1 — Primitive bushcraft (start of progression)

**Goal:** prevent punching trees with bare hands. Force the player through
grass → fiber → cordage → primitive axe → log.

**Files:**
- `events/BushcraftBreakEvents.java` — cancels log-break with non-axe tools
- `ModItems.java` — registers `plant_fiber`, `plant_cordage`, `primitive_axe`
- `items/PrimitiveAxeItem.java` — Tier 0 axe, durability 8, mining speed 1.5
- `glm/AddItemModifier.java` — generic GLM that injects an item into a loot table
- `glm/MultiplyItemModifier.java` — generic GLM that multiplies an existing item drop
- `ModLootModifiers.java` — registers the GLM codec
- `src/main/resources/data/forge/loot_modifiers/global_loot_modifiers.json` — GLM index
- `src/main/resources/data/glebthanwolves/loot_modifiers/*.json` — actual modifiers
- `src/main/resources/data/glebthanwolves/recipes/*.json` — cordage + primitive axe recipes
- Models, textures, lang files for the new items

**New items:**
- `glebthanwolves:plant_fiber`
- `glebthanwolves:plant_cordage`
- `glebthanwolves:primitive_axe` (durability 8, originally 30, lowered in 1.2)

**Drops (Global Loot Modifiers):**
- Tall grass / large fern → 15% Plant Fiber
- Short grass / regular fern → no fiber
- Leaves → 8% extra stick (on top of vanilla)
- Saplings dropped at 0.2× vanilla rate (5× rarer)

**Recipes** (all 2×2 inventory crafting, no workbench needed):
- 2 fiber stacked vertically → 1 cordage
- `[Flint][Cordage] / [_][Stick]` → 1 Primitive Axe

**Behaviour:** Bare-hand / sword / pickaxe / shovel can't break a vanilla log.
Primitive axe can. Implemented via `PlayerEvent.BreakSpeed` cancelling the
break when held item can't perform `ToolActions.AXE_DIG`. Creative is exempt.

---

## Phase 1.2 — Wood chunks (longer path to a workbench)

**Goal:** make crafting tables non-trivial.

**File:** `glm/*.json` for log loot tables; `src/main/resources/data/glebthanwolves/recipes/oak_planks_from_chunks.json`

**Changes:**
- All 8 vanilla log loot tables lose their log drop.
- Log drops 1–2 `wood_chunk` + 5% extra stick.
- Primitive axe durability 30 → 8.
- 2 wood chunks → 1 oak plank (shapeless 2×2). Wood chunk is generic regardless
  of source species.

**Math:** A workbench (4 planks) needs ~8 chunks ≈ 4–6 trees and at least one
primitive axe (durability 8 = 8 logs).

---

## Phase 1.3 — Hardcore world

**File:** `events/HardcoreEvents.java`, `events/WorldEvents.java`

1. **Daytime ×1.5.** `LevelTickEvent`: when day half of cycle (`getDayTime() %
   24000 < 12000`), advance `dayTime` by an extra 1 every 2 ticks. Respects
   `doDaylightCycle` gamerule.
2. **Raw food → Hunger I (12 sec).** On `LivingEntityUseItemEvent.Finish`,
   apply Hunger I 240 ticks if the consumed item is in the raw-meat/fish set:
   `beef, chicken, porkchop, mutton, rabbit, cod, salmon, tropical_fish`.
3. **+30% mob spawns at night.** `WorldEvents.onLevelTick`: every 60 ticks per
   online player, 50% chance to attempt a vanilla-rules-respecting spawn of
   `zombie/skeleton/spider/creeper` 24–48 blocks away. Light ≤ 7, sturdy floor,
   not visible to player. Respects `doMobSpawning`.
4. **Mobs +15% HP / +10% damage.** `EntityJoinLevelEvent` on every `Enemy`-
   implementing entity, applies permanent attribute modifiers (stable UUIDs).
   Mob is healed to its new max so it doesn't spawn pre-damaged.

---

## Phase 1.4 — Player nerfs (HP, sprint, sleep, golden apples)

**File:** `events/HardcoreEvents.java`

1. **Player max HP = 10 (5 hearts).** `MAX_HEALTH -10 ADDITION` permanent
   modifier on every level-join (login / respawn / dimension change). Idempotent
   via stable UUID `PLAYER_HP_CAP_UUID`.
2. **No sprint** (v2 implementation, the chosen one).
   - v1 (rejected): clamp `foodLevel = 6` every tick → broke vanilla regen and
     had client-prediction issues.
   - v2 (active): `+0.05 exhaustion/tick` while `foodLevel > 6` → drains hunger
     ~6× faster than vanilla. Once `foodLevel ≤ 6`, the vanilla client check
     blocks sprint engagement, and we stop the extra exhaustion. Vanilla regen
     stays intact (still requires `foodLevel ≥ 18`, achievable after eating).
3. **Zombies +20% movement speed.** `MOVEMENT_SPEED MULTIPLY_TOTAL +0.20` on
   every Zombie via `EntityJoinLevelEvent`.
4. **Phantoms keep spawning after sleep.** `PlayerWakeUpEvent` schedules a
   server task to push `Stats.TIME_SINCE_REST` to 72001 right after vanilla
   resets it to 0.
5. **20% chance to fail sleep.** `PlayerSleepInBedEvent.setResult(OTHER_PROBLEM)`
   with probability 0.20. Shows "You can't sleep right now".
6. **Golden / enchanted golden apple — effects stripped.** On
   `LivingEntityUseItemEvent.Finish`, immediately remove Regeneration,
   Absorption, Resistance, Fire Resistance after vanilla applies them. Hunger
   and saturation gain stay so the apple is still a snack.

---

## Phase 1.5 — Raw food deals damage

**File:** `events/HardcoreEvents.java`

- 1 raw meat/fish swallowed = -0.5 hearts (1 HP) `generic` damage.
- Stacks with the Hunger I from Phase 1.3.
- Implementation: `LivingEntityUseItemEvent.Finish`, `player.hurt(generic, 1.0F)`.
- Armor doesn't reduce it (treated as poisoning, not physical).
- With HP cap of 5 hearts, 5 raw fish in a row = death.

Affects the same set as Phase 1.3 raw-food check.

---

## Phase 1.6 — Environment & combat

**File:** `events/HardcoreEvents.java`

1. **Fall damage ×1.5.** `LivingHurtEvent`, `source.is(DamageTypes.FALL)`.
   With 5 hearts: 4 blocks ≈ 0.5 heart, 5 blocks ≈ 2 hearts, 8 blocks = death.
2. **Rain hurts.** -0.5 HP every 10 sec while under open sky during rain.
   `level.isRainingAt(pos.above())` correctly excludes desert/snow biomes
   (vanilla doesn't rain there) and roofs/leaves.
3. **Cold nights.** -0.5 HP every 30 sec when night (dayTime 13000-23000) and
   block-light at player's pos ≤ 7. A torch (light 14, falls off with distance)
   keeps you warm in ~6 block radius.
4. **Zombie grab.** 30% chance on a zombie hit to apply Slowness II for 3 sec.
   Makes mob escape much harder.
5. **Skeleton arrows ×1.5.** `LivingHurtEvent`, `source.getDirectEntity()
   instanceof AbstractArrow && source.getEntity() instanceof AbstractSkeleton`.
   Multiplies base damage. Pierces armor (the multiplier applies after armor
   reduction).

---

## Phase 1.7 — Aggressive iron golems + no trading

**File:** `events/HardcoreEvents.java`

1. **Iron golems target the player within 32 blocks.**
   - **v1 (didn't work):** added `NearestAttackableTargetGoal<Player>` to
     `targetSelector`. Got filtered by vanilla's `canAttack(player)` which
     returns `false` for player-built golems (`isPlayerCreated()`).
   - **v2 (works):** every 20 ticks via `LivingTickEvent`, set `golem.setTarget(
     nearestPlayer)` directly, bypassing the goal system. The default
     `MeleeAttackGoal` picks up the target and walks to attack.
   - Also `golem.setPlayerCreated(false)` on spawn for extra safety.
   - Range: 32 blocks. Re-check every 20 ticks. Target persists while alive
     and not in creative.
2. **No trading with villagers / wandering trader.**
   - `PlayerInteractEvent.EntityInteract` cancelled if right-click target is
     `AbstractVillager`. Trade GUI does not open.
   - Side effect: leads, name tags, hand-feeding zombie villagers also blocked.
   - Left-click (attack) still works; villagers can still be killed normally.

**Verified in dev client:** spawned a golem, switched to survival, golem killed
me ("Dev was slain by Iron Golem").

---

## Phase 1.8 — Wild wolves attack the player

**File:** `events/HardcoreEvents.java`

- `LivingTickEvent` on `Wolf`, `!isTame()` check, every 20 ticks.
- Range: 16 blocks (taiga / grove / snowy biomes feel dangerous).
- `wolf.setTarget(nearestPlayer)` + `wolf.setIsInterested(true)` (visual:
  pinned-back ears).
- Tamed wolves stay loyal.
- Vanilla pack-alert mechanic still works (one wolf aggroes → others follow).

---

## Phase 1.9 — Boats & oxygen

**File:** `events/HardcoreEvents.java`

1. **Boats ~×2 slower.**
   - Each player tick while in a `Boat` (incl. `ChestBoat`, bamboo raft):
     `dm.x *= 0.91`, `dm.z *= 0.91`. Vertical untouched.
   - Vanilla boat physics: accel `+0.04/tick`, friction `0.9` → `v_max ≈ 0.40`.
   - With our 0.91 factor: `v_max ≈ 0.20` (exactly halved in steady state).
   - Boats on ice also slow.
2. **Oxygen ×2 faster.**
   - Each tick, while head is underwater and player has neither Water Breathing
     nor a turtle helmet, `setAirSupply(air - 1)` — added to vanilla's `-1`,
     so total `-2/tick`. Full bar 300 → 0 in ~7.5 sec instead of 15.
   - Water Breathing potion fully bypasses the extra drain (vanilla logic).
   - Turtle helmet on dry land: nothing. Underwater: gives Water Breathing
     buff via vanilla, so also bypasses.
   - Respiration enchantment only affects vanilla's `-1`; our extra `-1` always
     drains (Respiration III drowns ~2× faster than vanilla too).

---

## Phase 1.10 — Movement complications (user picks #1, 2, 3, 4, 6, 7)

**File:** `events/HardcoreEvents.java`

1. **#1 Sneak ×0.5.** Dynamic `MOVEMENT_SPEED MULTIPLY_TOTAL -0.5` modifier
   added/removed by `isCrouching()`. `toggleSpeedModifier()` helper.
2. **#2 Snow / powder snow -25%.** Same dynamic-modifier pattern, `-0.25`,
   active when `isInPowderSnow` or block-at-feet/below is `Blocks.SNOW`.
3. **#3 Ice slippery ×1.5.** Per-tick velocity boost on ice/packed_ice/blue_ice/
   frosted_ice. Only multiplies horizontal velocity if the speed is between
   `0.05` and `0.50` so it doesn't snowball into infinite acceleration.
4. **#4 Encumbrance.** ≥10 non-empty inventory slots → Slowness I + Mining
   Fatigue I refreshed every 40 ticks for 60-tick duration. Counts every
   non-empty slot, so splitting a stack to evade detection doesn't help.
   Threshold lowered from 27 → 10 in the post-1.14 fix.
5. **#6 Swim -30%.** While `isInWater()` and not in a vehicle, multiply
   horizontal velocity by `0.85` each tick.
6. **#7 Climb x0.7.** While `onClimbable()` and not on ground, multiply
   vertical velocity by `0.7` each tick.

---

## Phase 1.11 — Predators (user picks #9, 10, 12, 13, 14)

**Files:** `events/HardcoreEvents.java`, `events/WorldEvents.java`

1. **#9 Silent creepers (15%).** On `EntityJoinLevelEvent`, with 15% chance
   tag a Creeper as silent and `setSilent(true)` so vanilla suppresses fuse
   hiss / step / hurt sounds. Stored via persistent data tag `GTWSilent`.
2. **#10 Witches everywhere.** Per online player, every 36000 ticks (30 min)
   with 50% chance, attempt a witch spawn 30–64 blocks away on a valid
   surface block (any biome, any time, in loaded chunks). Respects
   `doMobSpawning`. Implementation in `WorldEvents.trySpawnWitch`.
   Added in the post-1.14 fix.
3. **#12 Husk replaces zombie 10%.** On `EntityJoinLevelEvent`, if
   `entity.getClass() == Zombie.class` (excludes ZombieVillager / Husk /
   Drowned / Zombified Piglin subclasses), with 10% chance cancel the spawn
   and `addFreshEntity(husk)` at the same position with same difficulty.
4. **#13 Ghasts shoot extra fireballs.** `LivingTickEvent` on `Ghast`, every
   60 ticks if there's a target visible, 60% chance to fire one extra
   `LargeFireball` toward the target. Doesn't replace ghast's own AI volleys.
5. **#14 Headless creeper +radius.** On creeper spawn, 20% chance to flag a
   creeper as "headless" and bump `explosionRadius` to 5 (default 3) via
   reflection. Persisted via data tag so it survives reload.

---

## Phase 1.12 — Environmental hazards (user picks #15, 17, 18, 19, 20)

**Files:** `events/HardcoreEvents.java`, `events/WorldEvents.java`

1. **#15 Lightning ×3 in storms.** Per online player, every 400 ticks during
   thunder, 5% chance to strike a `LightningBolt` within ±3 blocks of the
   player if they're under open sky (`canSeeSky` check).
2. **#17 Desert heat at noon.** Damage 2.0 (1 heart) every 8 sec when:
   `isMidday(level)` (dayTime 5000–7000), no helmet equipped, biome's
   `getBaseTemperature() ≥ 1.5` (covers desert, badlands family, savannas),
   and `canSeeSky(pos)`. `damageSources().inFire()` source so HUD shows fire
   icon. Tightened in the post-1.14 fix (was 1.0 every 15 sec, strict
   `Biomes.DESERT` only, no sky check).
3. **#18 Cold in cold biomes.** Damage 2.0 (1 heart) every 30 sec when:
   not nighttime, no chest armor, `biome.coldEnoughToSnow(pos)`, and
   `canSeeSky(pos)`. `damageSources().freeze()` for snowflake icon.
4. **#19 Lava more aggressive.** `LivingHurtEvent`: if source is lava, multiply
   damage by 1.5. Each player tick while `isInLava()`, set
   `setRemainingFireTicks(max(current, 200))` to enforce minimum 10 sec of
   burning after stepping out.
5. **#20 Meteors at night.** `WorldEvents.onLevelTick`: per online player,
   every 200 ticks at night, 2% chance to spawn a `LargeFireball` (power 3)
   at y=200, 50–110 blocks horizontally from the player. Falls straight down
   under gravity, explodes on impact.

---

## Phase 1.13 — Perception & physiology (user picks #23, 24, 27)

**File:** `events/HardcoreEvents.java`

> Note: original Phase 1.13 also implemented #21 (rain blindness pulses), but
> the user clarified later they didn't want it. Removed in the post-1.14 fix.

1. **#23 Zombie infection.** On a Zombie hit (`LivingHurtEvent` with attacker
   `instanceof Zombie`), 15% chance to apply Hunger II for 6000 ticks (5 min).
2. **#24 Death fever.** `PlayerEvent.PlayerRespawnEvent` (non-end-conquest):
   apply Weakness I + Mining Fatigue I for 6000 ticks (5 min). Also resets
   the awake-tick counter to 0 (death counts as forced rest).
3. **#27 Sleep deprivation.** Per-player tick counter `GTWAwakeTicks` in
   `getPersistentData()`, increments every tick, resets on `PlayerWakeUpEvent`.
   Past 48000 ticks (2 vanilla days), every 100 ticks apply Slowness I +
   Weakness I (60-tick duration so they pulse). Doesn't conflict with the
   phantom-bump from Phase 1.4 (that writes to `Stats.TIME_SINCE_REST`).

---

## Phase 1.14 — World & items (user picks #36, 37, 39, 40, 41)

**File:** `events/HardcoreEvents.java`

1. **#36 50% XP keep on death.** `PlayerEvent.Clone` with `wasDeath=true`:
   `event.getOriginal().reviveCaps()` to safely read `totalExperience`, then
   `neu.giveExperiencePoints(50%)`. `invalidateCaps()` in finally block.
2. **#37 XP orbs expire in 30 sec.** `EntityJoinLevelEvent` on `ExperienceOrb`:
   reflectively set the private `age` field to `6000 - 600 = 5400`, so the
   orb has only 600 ticks (30 sec) left before vanilla's 6000-tick lifespan
   expires.
3. **#39 Swamp slow.** Player tick: every 40 ticks if `isInWater()` and biome
   is `Biomes.SWAMP` or `Biomes.MANGROVE_SWAMP`, apply Slowness I for 60 ticks.
4. **#40 Cactus ×2 damage.** `LivingHurtEvent` with `DamageTypes.CACTUS`:
   `event.setAmount(amount * 2.0F)`.
5. **#41 Sweet berries ×3 damage.** Same with `DamageTypes.SWEET_BERRY_BUSH`,
   factor 3.0.

---

## Post-1.14 fix (commit `1aeff4c`)

User feedback after Phase 1.14:
- Encumbrance threshold of 27 was too lenient → lowered to **10**.
- Endermen 16-block aggro: user said "I didn't ask for this" (confusion in
  the brainstorm numbering — they picked #10 = witches, I implemented #8 =
  endermen by mistake). **Removed.**
- Rain fog Blindness: user said "I didn't ask for this". **Removed.**
- Witches everywhere (#10): was **missing**, added. See Phase 1.11 entry.
- Desert heat didn't appear to fire. Three fixes:
  - Damage `1.0F → 2.0F` (1 heart, clearly visible past slow regen).
  - Interval `15 sec → 8 sec`.
  - Damage source `generic() → inFire()` (fire icon in HUD).
  - Biome check: strict `Biomes.DESERT` → temperature `≥ 1.5F` (covers
    desert + badlands + savannas).
  - Added `canSeeSky(pos)` so being under a roof shields you.
- Cold damage got matching treatment: `2.0F` damage, `freeze()` source,
  `canSeeSky` check.

---

## Phase 2.0 — Armor gate (start of progression redesign)

**Files:**
- `events/HardcoreEvents.java`
- `src/main/resources/data/minecraft/recipes/iron_*.json`
- `src/main/resources/data/minecraft/recipes/golden_*.json`
- `src/main/resources/data/minecraft/recipes/diamond_*.json`

**Goal:** begin replacing vanilla progression by making strong found/crafted
armor unusable until the mod adds its own progression path.

**Allowed armor:** leather and chainmail.

**Forbidden armor:** iron, gold, diamond, netherite, turtle helmet, and any
other armor item whose material is not leather/chainmail.

**Changes:**
1. Vanilla crafting recipes for iron/gold/diamond helmets, chestplates,
   leggings, and boots are overridden with `forge:false` recipe files, so they
   do not load.
2. `PlayerInteractEvent.RightClickItem`: right-clicking forbidden armor from
   hand is cancelled with `InteractionResult.FAIL`.
3. `LivingEquipmentChangeEvent`: if a player equips forbidden armor by any
   route, the slot is restored to the previous item and the forbidden armor is
   returned to inventory or dropped if inventory is full.

**Important note:** vanilla 1.20.1 has no netherite-armor crafting/smithing
recipe JSONs in the jar; netherite armor is blocked by the equip gate instead.

---

## Phase 2.1 — Tool craft gate

**Files (datapack overrides only):**
- `src/main/resources/data/minecraft/recipes/<tier>_<tool>.json` — 25 files
  (5 tiers × 5 tool types: wooden / stone / iron / golden / diamond ×
  pickaxe / axe / shovel / hoe / sword)
- `src/main/resources/data/minecraft/recipes/netherite_<tool>_smithing.json` — 5
  files (pickaxe / axe / shovel / hoe / sword via smithing transform)
- `src/main/resources/data/minecraft/recipes/bow.json`
- `src/main/resources/data/minecraft/recipes/crossbow.json`
- `src/main/resources/data/minecraft/recipes/flint_and_steel.json`
- `src/main/resources/data/minecraft/recipes/shield.json`

**Goal:** force the player off the vanilla tool tree and onto a custom GTW
progression. The vanilla tool items still exist (so found loot, mob drops,
fishing, structures keep working), but **the player can never craft them**.

**Forbidden via crafting gate:** wooden / stone / iron / golden / diamond /
netherite × pickaxe / axe / shovel / hoe / sword, plus bow, crossbow, flint
and steel, shield. **34 vanilla recipes** total turned off.

**Still craftable in vanilla:** shears, fishing rod, brush, bucket, compass,
clock, spyglass, lead, name tag (all non-weapon utility items).

**Mod tools unaffected:** `glebthanwolves:primitive_axe` and any future
`glebthanwolves:*` tool items remain craftable as long as their recipes use
the `glebthanwolves:` namespace. We only override the `minecraft:` recipe
files, so mod recipes are unaffected.

**Implementation:** identical pattern to Phase 2.0 armor gate — each recipe
JSON has `forge:conditions: [{type: forge:false}]` so Forge skips loading
the recipe entirely. Body of the file uses a dummy `crafting_shapeless`
result of `minecraft:barrier` to keep the JSON valid. No Java code changes.

**Important notes:**
- The recipe files **must** have the same path as the vanilla recipe ID. The
  filenames here match `data/minecraft/recipes/<id>.json` for each banned
  recipe.
- For netherite tools the vanilla recipe IDs end in `_smithing` (e.g.
  `netherite_pickaxe_smithing`). We match this exactly.
- Tools the player **finds** (chests, mob drops, fishing, structure loot)
  are still usable. Phase 2.2 will weaken those.

---

## Phase 2.2 — Crude stone tools (first GTW tier)

**Files:**
- `items/GtwTiers.java` — defines `GtwTiers.GTW_STONE` (durability 70, mining
  speed 3.0, +1.0 attack damage, mining level 1, enchantability 5, repair
  ingredient cobblestone). Same level/damage/repair as vanilla `Tiers.STONE`,
  but ~53% durability and 75% mining speed.
- `ModItems.java` — registers `stone_pickaxe`, `stone_axe`, `stone_sword`,
  `stone_shovel`, `stone_hoe` using vanilla `PickaxeItem` / `AxeItem` /
  `SwordItem` / `ShovelItem` / `HoeItem` with `GTW_STONE` tier. Damage / attack
  speed constructor args identical to vanilla `Items.STONE_*`.
- `ModCreativeTabs.java` — appends the 5 stone tools to the main GTW tab.
- `assets/glebthanwolves/lang/en_us.json` + `ru_ru.json` — adds 5 lang entries
  per language ("Crude Stone *" / "Грубый каменный *").
- `assets/glebthanwolves/models/item/stone_*.json` — 5 item models. They
  reuse vanilla textures (`minecraft:item/stone_*`), so no new PNGs are
  shipped — visually identical to vanilla stone tools, only the lang label
  marks them as "Crude".
- `data/glebthanwolves/recipes/stone_*.json` — 5 shaped recipes on a 3×3
  crafting table. All 5 require **plant_cordage** as a binding ingredient
  (replacing one of vanilla's two sticks). `stone_sword` uses cordage instead
  of a stick entirely (2 cobble + 1 cordage). The other four follow vanilla
  shapes but with the bottom stick swapped for cordage.

**Goal:** start the GTW progression replacement. The player has no vanilla
crafting path to any tool after Phase 2.1, so Phase 2.2 hands them their
first real (mineable-tier) toolset, gated through the bushcraft chain
(plant_cordage). These tools are deliberately weaker than vanilla stone:
half durability, 25% slower digging.

**Stats per tool** (final HUD numbers — match vanilla stone exactly):

| Item | Durability | Mining speed | Attack damage | Attack speed |
|---|---|---|---|---|
| `stone_pickaxe` | 70 | 3.0 | 3 | -2.8 |
| `stone_axe`     | 70 | 3.0 | 9 | -3.2 |
| `stone_sword`   | 70 | 3.0 | 5 | -2.4 |
| `stone_shovel`  | 70 | 3.0 | 3.5 | -3.0 |
| `stone_hoe`     | 70 | 3.0 | 1 | -2.0 |

**Open issue — first cobblestone:** Phase 2.1 disabled vanilla wooden /
stone pickaxe crafting and Phase 2.2's own `stone_pickaxe` requires
cobblestone in its recipe. There is currently no in-mod path to break
stone unless the player finds a vanilla pickaxe in chest / mob loot.
This is tracked in `ROADMAP.md` under "Phase 2.2 — open issue".

**Did not change:** `HardcoreEvents.java`, `WorldEvents.java`,
`BushcraftBreakEvents.java`, `PrimitiveAxeItem.java`, GLM modifiers,
the 46 vanilla recipe overrides, and all of Phase 1.x mechanics.

**Recipe advancements:** not generated. New recipes will not auto-unlock
in the player's recipe book until a `RecipeProvider` (datagen) emits the
matching `data/glebthanwolves/advancements/recipes/*.json` files. Tracked
under "Phase 2.2 — open issue" in ROADMAP.

---

## Phase 2.3a — Mining gate (default-deny scaffold, EMPTY whitelist)

**Concept:** instead of "all blocks breakable, but some require a higher
tool tier" (vanilla), the mod inverts the rule: **all blocks are
unbreakable by default**, and each tool tier has an explicit datapack
whitelist of blocks it is allowed to break. This makes blocks themselves
into a designed resource — adding a block to the game now requires an
explicit decision about which tool tier may harvest it.

This is the first half of the user's "blocks-as-currency" redesign. The
second half (every block drops *shards* instead of itself, recombined via
crafting) lands in Phase 2.3b. Variant for shards: **A — one universal
`stone_shard`** (decided in chat); separate per-block shards may be
introduced later only for items that need them.

**This PR ships the gate with an EMPTY whitelist.** The infrastructure is
fully wired up, but no block is allowed for any tier — by user decision
("я написал полный вайтлист, это значит ПОЛНЫЙ, всё блокируй"). After
this PR is merged, blocks will be unlocked one-by-one in follow-up PRs as
gameplay design progresses.

**Files:**
- `events/MiningGate.java` — Forge event handler. Subscribes to two events:
  - `PlayerEvent.BreakSpeed` → if the held tool's tier cannot break the
    block, sets break speed to `0.0F` (block appears unbreakable, no
    crack animation, no progress bar). This is the visible feedback.
  - `BlockEvent.BreakEvent` → defense-in-depth cancel if a break would
    somehow proceed despite speed=0 (other mods, NBT shenanigans, etc.).
  - Tool tier is `TieredItem.getTier().getLevel() + 1`, so vanilla
    WOOD=1=primitive, STONE=2, IRON=3, DIAMOND=4, NETHERITE=5. Held items
    that aren't `TieredItem` (bows, fiber, cordage, bare hand) report
    tier 0 (hand). GTW `stone_*` and vanilla `stone_*` both map to tier
    2, so found vanilla tools work the same as crafted GTW tools (until
    Phase 2.3 — found tools weaker — replaces them).
  - Creative players bypass the gate entirely.
- `data/glebthanwolves/tags/blocks/breakable_by/hand.json` — empty
  (`"values": []`).
- `data/glebthanwolves/tags/blocks/breakable_by/primitive.json` — only
  inherits from `hand` via `"#glebthanwolves:breakable_by/hand"`. Same
  effective content (empty).
- `data/glebthanwolves/tags/blocks/breakable_by/stone.json` — only
  inherits from `primitive`. Empty.
- `data/glebthanwolves/tags/blocks/breakable_by/iron.json` — only
  inherits from `stone`. Empty.
- `data/glebthanwolves/tags/blocks/breakable_by/diamond.json` — only
  inherits from `iron`. Empty.
- `data/glebthanwolves/tags/blocks/breakable_by/netherite.json` — only
  inherits from `diamond`. Empty.
- `GlebThanWolves.java` — registers `new MiningGate()` on the Forge event
  bus alongside the existing handlers.

The tag-include chain (`netherite ⊃ diamond ⊃ iron ⊃ stone ⊃ primitive ⊃
hand`) is wired up so that adding a block to (e.g.) `stone.json` automatically
makes it breakable by stone-, iron-, diamond-, and netherite-tier tools too.
That means future PRs only edit a single tag at the appropriate tier.

**Effect on gameplay (intended, per user request):**
- A surviving player at spawn can break **nothing**, including grass,
  dirt, sand, leaves, logs, etc. Plant_fiber (the entry to bushcraft) is
  unobtainable until grass is added to the hand whitelist.
- Mob drops, fishing, chest loot, spawn inventory still work. The gate
  only restricts block breaking.
- Crafting still works.
- Creative is fully unaffected.

This is **deliberately a soft-lock at spawn** — it's the starting point
from which we will design out, allowing one block at a time.

**Interaction with other mechanics:**
- `BushcraftBreakEvents` (Phase 1.1, "logs need axe") still runs as a
  redundant safety net. With logs now blocked by hand-tier too (they're
  in the empty whitelist), the bushcraft check is moot — but it stays
  for now, in case logs are unlocked at hand-tier later.
- Vanilla `correctToolForDrops` is left untouched, so even if a tool
  *can break* a block under the gate, the block may still drop nothing
  if the tool is the wrong type (e.g. iron shovel breaks iron_ore at
  iron tier but yields no ingot — same as vanilla).

**Open issues carried into Phase 2.3b+:**
- **Whitelist needs to be filled.** Until at least short_grass (or
  similar) is unlocked at hand tier, a player cannot start the bushcraft
  chain. Decide first batch of allowed blocks per tier.
- **No silk-touch carve-out yet.** The "silk-touch bypasses shards" rule
  (Q6) doesn't matter until Phase 2.3b ships shards.

---

## Phase 2.2.x — Crude wooden + iron tools (finishing the GTW tier set)

User asked to finish the GTW tool set up through iron before resuming
the Phase 2.3 progression work, with the same "Crude" pattern as Phase
2.2 (vanilla baselines, ~50% durability, ~75% mining speed, vanilla
textures reused). 10 new tools land in this PR (5 wood + 5 iron). The
existing `primitive_axe` (Phase 1.1) is kept as a separate starter item.

**Files:**
- `items/GtwTiers.java` — adds two new tiers next to `GTW_STONE`:
  - `GTW_WOODEN` — durability 30 (vs vanilla 59), mining speed 1.5
    (vs 2.0), +0 attack damage, mining level 0, enchantability 15,
    repair = `oak_planks`. Mirrors `Tiers.WOOD`.
  - `GTW_IRON` — durability 130 (vs vanilla 250), mining speed 4.5
    (vs 6.0), +2.0 attack damage, mining level 2, enchantability 14,
    repair = `iron_ingot`. Mirrors `Tiers.IRON`.
- `ModItems.java` — registers 10 new items: `wooden_pickaxe`, `_axe`,
  `_sword`, `_shovel`, `_hoe` (using `GTW_WOODEN`) and
  `iron_pickaxe`, `_axe`, `_sword`, `_shovel`, `_hoe` (using
  `GTW_IRON`). Constructor args (damage / attack speed) are vanilla
  baselines for each tool type, so HUD attack stats match vanilla
  exactly.
- `ModCreativeTabs.java` — appends 10 tools to the main GTW tab in
  display order (primitive → wooden → stone → iron).
- `assets/glebthanwolves/lang/en_us.json` + `ru_ru.json` — adds 10 lang
  entries each ("Crude Wooden *" / "Crude Iron *" and Russian
  equivalents).
- `assets/glebthanwolves/models/item/{wooden,iron}_*.json` — 10 item
  models. They reuse vanilla textures (`minecraft:item/wooden_*` and
  `minecraft:item/iron_*`), so no new PNGs are shipped — visually
  identical to vanilla wood/iron tools, only the lang label marks them
  as "Crude". Per user note: "модельки можешь взять как у ванильных
  инструментов(потом изменим)".
- `data/glebthanwolves/recipes/{wooden,iron}_*.json` — 10 shaped recipes
  on a 3×3 crafting table. Same pattern as Phase 2.2 stone recipes:
  - Wooden recipes use `#minecraft:planks` tag (any planks variant works).
  - Iron recipes use `minecraft:iron_ingot`.
  - All require `glebthanwolves:plant_cordage` as the binding ingredient
    (replacing one stick), gating the entire tier set behind the
    bushcraft chain.
  - Sword variants use cordage as the handle (no stick).

**Stats per tool** (final HUD numbers — match vanilla wood/iron exactly):

| Item | Durability | Mining speed | Attack damage | Attack speed |
|---|---|---|---|---|
| `wooden_pickaxe` | 30 | 1.5 | 2 | -2.8 |
| `wooden_axe`     | 30 | 1.5 | 7 | -3.2 |
| `wooden_sword`   | 30 | 1.5 | 4 | -2.4 |
| `wooden_shovel`  | 30 | 1.5 | 2.5 | -3.0 |
| `wooden_hoe`     | 30 | 1.5 | 1 | -3.0 |
| `iron_pickaxe`   | 130 | 4.5 | 4 | -2.8 |
| `iron_axe`       | 130 | 4.5 | 9 | -3.1 |
| `iron_sword`     | 130 | 4.5 | 6 | -2.4 |
| `iron_shovel`    | 130 | 4.5 | 4.5 | -3.0 |
| `iron_hoe`       | 130 | 4.5 | 1 | -1.0 |

(HUD attack damage = constructor baseline + tier bonus + 1.0 player base.)

**Did not change:** `MiningGate.java`, the 6 `breakable_by/*` tags
(still empty per Phase 2.3a), `HardcoreEvents.java`, `WorldEvents.java`,
`BushcraftBreakEvents.java`, `PrimitiveAxeItem.java`, GLM modifiers,
the 46 vanilla recipe overrides from Phase 2.0/2.1.

**Open issues carried forward:**
- Same as Phase 2.2: no recipe advancements emitted, models reuse
  vanilla textures.
- Mining Gate still empty — these tools can be crafted but cannot
  break any block until the whitelist gets populated.

---

## Phase 2.2.y — Crude diamond tools (top of the GTW tier ladder)

User asked to finish the diamond tier before starting the big shard /
mining-logic work, mirroring the wooden / stone / iron pattern from
Phase 2.2 / 2.2.x.

**Files:**
- `items/GtwTiers.java` — adds `GTW_DIAMOND` next to the wooden / stone
  / iron tiers:
  - `GTW_DIAMOND` — durability 780 (vs vanilla 1561), mining speed 6.0
    (vs 8.0), +3.0 attack damage, mining level 3, enchantability 10,
    repair = `diamond`. Mirrors `Tiers.DIAMOND`.
- `ModItems.java` — registers 5 new items: `diamond_pickaxe`,
  `diamond_axe`, `diamond_sword`, `diamond_shovel`, `diamond_hoe`. As
  with the other tiers, constructor args (damage / attack speed) are
  vanilla baselines so HUD attack stats match vanilla diamond exactly.
- `ModCreativeTabs.java` — appends the 5 new items to the main tab
  after the iron block (final order: primitive → wooden → stone → iron
  → diamond).
- `assets/glebthanwolves/lang/en_us.json` + `ru_ru.json` — 5 new lang
  entries each ("Crude Diamond *" / "Грубый алмазный *" etc.).
- `assets/glebthanwolves/models/item/diamond_*.json` — 5 item models,
  reusing vanilla textures (`minecraft:item/diamond_*`).
- `data/glebthanwolves/recipes/diamond_*.json` — 5 shaped recipes on a
  3×3 crafting table, same shape as Phase 2.2 / 2.2.x. Use
  `minecraft:diamond` + stick + `glebthanwolves:plant_cordage`. Sword
  has cordage as the handle (no stick).

**Stats per tool** (HUD numbers — match vanilla diamond exactly):

| Item | Durability | Mining speed | Attack damage | Attack speed |
|---|---|---|---|---|
| `diamond_pickaxe` | 780 | 6.0 | 5 | -2.8 |
| `diamond_axe`     | 780 | 6.0 | 9 | -3.0 |
| `diamond_sword`   | 780 | 6.0 | 7 | -2.4 |
| `diamond_shovel`  | 780 | 6.0 | 5.5 | -3.0 |
| `diamond_hoe`     | 780 | 6.0 | 1 | 0.0 |

(HUD attack damage = constructor baseline + tier bonus +3.0 + 1.0
player base.)

**Did not change:** `MiningGate.java`, the 6 `breakable_by/*` tags
(still empty per Phase 2.3a), `HardcoreEvents.java`, `WorldEvents.java`,
`BushcraftBreakEvents.java`, `PrimitiveAxeItem.java`, GLM modifiers,
vanilla recipe overrides from Phase 2.0 / 2.1, all earlier Phase 2.2 /
2.2.x / 2.3a artifacts.

**Open issues carried forward:**
- Same as Phase 2.2 / 2.2.x: no recipe advancements emitted, models
  reuse vanilla textures.
- Mining Gate still empty — these tools (and all earlier GTW tools)
  can be crafted but cannot break any block until the whitelist gets
  populated. That is the goal of the next, much larger PR (Phase
  2.3b — shards + 24-category mining logic).
- No netherite tier yet. Diamond is the current ceiling.

---

## Phase 2.3b — Block shards (DELIVERED, then ROLLED BACK)

**Branch:** `devin/1778192365-phase-2-3b-impl`
**Design PR:** [#7](https://github.com/gghyrmrwf/gleb-than-wolves/pull/7) — abandoned
**Impl PR:** [#8](https://github.com/gghyrmrwf/gleb-than-wolves/pull/8) — abandoned

**Built:** 18 shard categories, 151 recombine recipes, populated mining-gate,
custom GLM codec, item rename via `MissingMappingsEvent`, `MINING_DESIGN.md`
spec doc.

**Failed because:**
1. One shard mapped to many recipes → only first alphabetically fired (rest dead).
2. Overgeneralized categories (`compressed_metal_fragment`, `precious_fragment`)
   produced thematically nonsensical outputs.
3. Default-deny `MiningGate` whitelist made too many vanilla blocks unminable
   in practice ("blocks just don't break").

**User reaction:** "полный мусор без логики, максимальный бред и халатность".
Demanded full rollback to Phase 2.2.x state.

---

## Phase 2.3 — Tier tightening via vanilla tag overrides (current)

**Branch:** `devin/1778243030-phase-2-3-tier-tighten`
**PR:** [#9](https://github.com/gghyrmrwf/gleb-than-wolves/pull/9)
**Replaces:** PR #6 (diamond tools, cherry-picked in), PR #7, PR #8.

**Approach:**
- Removed `MiningGate.java` + all `breakable_by/*.json` tags.
- Restored vanilla mining semantics (wrong tool breaks block silently, no drop).
- Cherry-picked diamond tools (`d61ac87`).
- Added datapack overrides on vanilla `minecraft:needs_*_tool` tags.

**`needs_iron_tool` (require iron pickaxe to drop):**
- copper, deepslate_copper (was stone)
- lapis, deepslate_lapis (was stone)
- nether_gold (was wood)
- amethyst_block + budding + 4 cluster/bud states (was wood)
- end_stone + end_stone_bricks + decorative variants (was wood)
- bell, anvil + chipped + damaged (was wood)
- shulker_box + 16 dyed colors (was wood)

**`needs_stone_tool` (require stone pickaxe to drop):**
- nether_quartz (was wood)
- magma_block (was wood)

**Glowstone NOT tightened** — vanilla block has no `requiresCorrectToolForDrops`,
so tag-based tightening has no effect. Would require mixin to fix.

**Progression:**
```
hand → primitive_axe → wood pickaxe → stone pickaxe (cobblestone, coal)
  → iron pickaxe (iron, copper, lapis, nether_gold, amethyst, end_stone, etc.)
  → diamond pickaxe (obsidian, ancient_debris)
  → netherite (placeholder, not yet implemented)
```

**No shards in this PR.** Per user: shards will be added one at a time, only
for "important" blocks, after this mining-gate is validated stable.

---

## Removed experiment — Phase 1.15 cave danger

Phase 1.15 briefly added deep-cave Darkness/Weakness and rare cave ambushes.
The user was not confident it worked and decided it was not very needed, so
the mechanic was removed before moving into Phase 2 armor/progression work.

---

## Files at end of Phase 2.0 armor gate

```
src/main/java/com/gghyrmrwf/glebthanwolves/
├── GlebThanWolves.java                 (mod entrypoint, registers handlers)
├── ModItems.java                       (registers fiber/cordage/axe/wood_chunk)
├── ModCreativeTabs.java                (creative tab for new items)
├── ModLootModifiers.java               (registers GLM codec)
├── events/
│   ├── BushcraftBreakEvents.java       (Phase 1.1: cancel log break)
│   ├── HardcoreEvents.java             (Phase 1.3+: most mechanics live here, plus Phase 2.0 armor gate)
│   └── WorldEvents.java                (Phase 1.3, 1.11, 1.12: world-level ticks)
├── glm/
│   ├── AddItemModifier.java            (GLM codec — inject item into loot table)
│   └── MultiplyItemModifier.java       (GLM codec — multiply existing drop)
└── items/
    └── PrimitiveAxeItem.java           (Phase 1.1: tier 0 axe)

src/main/resources/
├── META-INF/mods.toml
├── pack.mcmeta
├── assets/glebthanwolves/
│   ├── lang/en_us.json, ru_ru.json     (item names)
│   └── models/item/*.json              (item models)
└── data/
    ├── forge/loot_modifiers/global_loot_modifiers.json
    └── glebthanwolves/
        ├── loot_modifiers/             (GLM rules: fiber drop, log replace, etc.)
        └── recipes/                    (cordage, primitive axe, wood chunk → plank)
    └── minecraft/recipes/              (vanilla recipe overrides for banned armor)
```

---

## Vanilla constants the mod relies on

| Constant | Value | Used for |
|---|---|---|
| Sprint food threshold | foodLevel ≤ 6 | Vanilla blocks sprint here (Phase 1.4 mechanism) |
| Regen food threshold | foodLevel ≥ 18 | Vanilla heals here |
| Phantom threshold | 72000 ticks no rest | Phase 1.4 phantom-bump |
| Day length | 12000 ticks day half | Phase 1.3 daytime ×1.5 boost |
| Boat physics | accel 0.04, friction 0.9 → v_max ≈ 0.40 | Phase 1.9 halving |
| Air supply | max 300, drain 1/tick, damage at -20 | Phase 1.9 doubling |
| Light level | BLOCK 0–15 (torch 14, fire 15) | Phase 1.6 cold-night |
| Day-time night | 13000–23000 | Phase 1.6 cold-night gating |
| XP orb age max | 6000 ticks (5 min) | Phase 1.14 reduction to 600 (30 sec) |

---

## Mod compatibility (logical / not tested)

**Compatible:**
- JEI — recipe browser, must-have
- AppleSkin — exact food/saturation display
- Quark — small QoL features
- Tough As Nails — thirst + temperature (synergistic)
- Farmer's Delight — extended cooking
- Sophisticated Backpacks — backpacks
- Aquaculture 2 — fishing
- Dynamic Lights — held torch lights up world

**Not compatible (overlapping mechanics):**
- Scaling Health — second HP/DMG buff stacks badly with our +15%/+10%
- First Aid — conflicts with our HP cap of 10
- Origins — overrides MAX_HEALTH
- Sleep Tight — interferes with our 20% sleep-fail
- Better Combat — may shadow our +10% melee damage
