# Gleb Than Wolves — Changelog

**Minecraft 1.20.1 / Forge 47.2.0 / Java 17**
Mod ID: `glebthanwolves` · Package: `com.gghyrmrwf.glebthanwolves`
Repo: https://github.com/gghyrmrwf/gleb-than-wolves
Active PR: https://github.com/gghyrmrwf/gleb-than-wolves/pull/2
Active branch: `devin/1777925074-phase-1-1-bushcraft`

This changelog covers everything from Phase 0 (empty mod scaffold) through Phase
2.1. All numeric parameters live as named constants
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
- Phase 2.1 dark night was build-verified locally; needs in-game testing for
  visual darkness, no-sleep behavior, and the 5-minute darkness kill.
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

## Phase 2.1 — Every 5th night is a dark night

**Files:**
- `events/DarkNightEvents.java`
- `client/DarkNightClientEvents.java`
- `data/glebthanwolves/damage_type/the_darkness.json`
- `data/minecraft/tags/damage_type/bypasses_armor.json`
- `data/minecraft/tags/damage_type/bypasses_invulnerability.json`
- `assets/glebthanwolves/lang/*.json`

**Goal:** every fifth overworld night should feel like a real supernatural
darkness event, not a vanilla Blindness effect. The moon remains visible, but
world fog and lightmap are darkened.

**Schedule:** day number is based on `level.getDayTime() / 24000 + 1`, so
sleep-skipped nights still advance the calendar correctly. Dark nights are days
5, 10, 15, etc. during `13000..23000`.

**Changes:**
1. Client-side fog color/distance and lightmap are darkened only on dark nights.
   No `Blindness`/`Darkness` potion effect is applied.
2. Sleeping is blocked during a dark night with an actionbar message.
3. If a survival/adventure player stands in block light ≤ 1, cannot see sky,
   and remains there for 6000 ticks / 5 minutes during a dark night, the custom
   `glebthanwolves:the_darkness` damage type kills them.
4. The death message is localized as a mysterious disappearance:
   `%1$s disappeared into the dark` / `%1$s исчез во тьме`.

**Important note:** direct moon brightness is handled by leaving the sky render
intact and darkening the world light/fog, rather than drawing an overlay over
the whole screen.

---

## Removed experiment — Phase 1.15 cave danger

Phase 1.15 briefly added deep-cave Darkness/Weakness and rare cave ambushes.
The user was not confident it worked and decided it was not very needed, so
the mechanic was removed before moving into Phase 2 armor/progression work.

---

## Files at end of Phase 2.1 dark night

```
src/main/java/com/gghyrmrwf/glebthanwolves/
├── GlebThanWolves.java                 (mod entrypoint, registers handlers)
├── ModItems.java                       (registers fiber/cordage/axe/wood_chunk)
├── ModCreativeTabs.java                (creative tab for new items)
├── ModLootModifiers.java               (registers GLM codec)
├── events/
│   ├── BushcraftBreakEvents.java       (Phase 1.1: cancel log break)
│   ├── DarkNightEvents.java            (Phase 2.1: every-5th-night schedule, no sleep, dark-exposure kill)
│   ├── HardcoreEvents.java             (Phase 1.3+: most mechanics live here, plus Phase 2.0 armor gate)
│   └── WorldEvents.java                (Phase 1.3, 1.11, 1.12: world-level ticks)
├── client/
│   └── DarkNightClientEvents.java      (Phase 2.1: fog/lightmap darkness)
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
