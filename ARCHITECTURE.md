# Architecture

This document describes the structure of the codebase, the conventions used,
the Forge events the mod hooks into, and the gotchas you need to know to
extend it safely.

---

## Directory layout

```
gleb-than-wolves/
├── build.gradle                # Forge MDK gradle script
├── gradle.properties           # mod_id, version, MC version, Forge version
├── settings.gradle
├── src/main/
│   ├── java/com/gghyrmrwf/glebthanwolves/
│   │   ├── GlebThanWolves.java                 (1) mod entrypoint
│   │   ├── ModItems.java                       (2) deferred-register items
│   │   ├── ModCreativeTabs.java                (2) creative tab
│   │   ├── ModLootModifiers.java               (2) GLM codec registration
│   │   ├── events/
│   │   │   ├── BushcraftBreakEvents.java       (3) bushcraft
│   │   │   ├── DarkNightEvents.java            (3) dark-night server rules
│   │   │   ├── HardcoreEvents.java             (3) most mechanics
│   │   │   └── WorldEvents.java                (3) world-tick mechanics
│   │   ├── client/
│   │   │   └── DarkNightClientEvents.java      (3) dark-night fog/lightmap
│   │   ├── glm/
│   │   │   ├── AddItemModifier.java            (4) generic loot modifier
│   │   │   └── MultiplyItemModifier.java       (4) generic loot modifier
│   │   └── items/
│   │       └── PrimitiveAxeItem.java           (5) custom axe
│   └── resources/
│       ├── META-INF/mods.toml                  (6) Forge mod manifest
│       ├── pack.mcmeta                         (6) datapack manifest
│       ├── assets/glebthanwolves/
│       │   ├── lang/{en_us,ru_ru}.json         (7) item names
│       │   └── models/item/*.json              (7) item models
│       └── data/
│           ├── forge/loot_modifiers/global_loot_modifiers.json   (8) GLM index
│           └── glebthanwolves/
│               ├── loot_modifiers/*.json       (8) GLM rules
│               └── recipes/*.json              (9) crafting recipes
└── build/libs/glebthanwolves-1.0.0.jar         (output of `./gradlew build`)
```

### What goes where (callouts)

1. **`GlebThanWolves.java`** — registered via `@Mod(GlebThanWolves.MODID)`.
   In its constructor it grabs the `MOD` event bus (for setup events) and the
   `FORGE` event bus, and registers our event handler classes.
2. **Mod\*.java** — deferred-register helpers using
   `DeferredRegister<Item>` and similar. Standard Forge boilerplate.
3. **`events/*.java`** — these classes are instantiated and registered onto
   `MinecraftForge.EVENT_BUS` in the mod constructor. **All gameplay mechanics
   live here.** Add new mechanics by adding methods annotated `@SubscribeEvent`
   to one of these files (or create a new file in this package and register it).
4. **`glm/`** — Forge **Global Loot Modifier** infrastructure. Two generic
   codecs:
   - `AddItemModifier` — appends a stack to a loot table's drops with optional
     conditions.
   - `MultiplyItemModifier` — multiplies an existing item's count in a drop.
   Modifiers themselves are JSON files under
   `data/glebthanwolves/loot_modifiers/` and listed in
   `data/forge/loot_modifiers/global_loot_modifiers.json`.
5. **`items/`** — concrete `Item` subclasses for non-trivial behaviour.
   `PrimitiveAxeItem` exists because the axe needs custom durability /
   tier-level binding. Plain items (`plant_fiber`, `wood_chunk`) are
   registered as `Item(new Item.Properties())` in `ModItems` directly.
6. **`mods.toml`** — Forge expects this to declare the mod id, name, version,
   and dependency on Forge / Minecraft. `pack.mcmeta` declares the included
   datapack.
7. **`assets/`** — client-side resources. Models follow the standard
   `parent: "minecraft:item/generated"` pattern with a single layer 0 texture.
8. **GLM JSON** — modifies vanilla loot tables without overwriting them. Each
   modifier file has `type: "glebthanwolves:add_item"` (or `multiply_item`)
   and a list of conditions (block ID, herb species, etc.).
9. **`recipes/`** — vanilla recipe JSON. We use 2×2 inventory crafting
   (`crafting_shaped` with patterns ≤ 2×2) so recipes work without a
   workbench.

---

## Build & deploy

```bash
# from repo root
./gradlew clean              # nuke build/
./gradlew compileJava        # type-check only (~5–10 sec)
./gradlew build              # full build, lint + test, produces .jar
./gradlew runClient          # launch dev client (slow, ~3 min first time)
./gradlew runServer          # launch dev server
```

Output: `build/libs/glebthanwolves-1.0.0.jar`. Drop into a Forge 1.20.1
(47.2.0) `mods/` folder.

For development:
- Java 17 (Forge 1.20.1 requires this exactly).
- Gradle 8.1.1 wrapper bundled.
- IDE: IntelliJ or VS Code with the official Java extension. Run
  `./gradlew genIntellijRuns` or `./gradlew genVSCodeRuns` to generate run
  configs.

---

## Forge events used

| Event | Phase introduced | Purpose |
|---|---|---|
| `TickEvent.PlayerTickEvent` | 1.4, 2.1 | Hunger drain, movement scaling, swim/climb scaling, encumbrance, rain damage, cold damage, lava-on-fire, sleep deprivation, swamp slow; dark-night exposure timer |
| `TickEvent.LevelTickEvent` | 1.3 | Day-time ×1.5, night extra spawns, meteors, witches |
| `LivingEvent.LivingTickEvent` | 1.7 | Iron golem aggro, wolf aggro, ghast extra fireballs |
| `EntityJoinLevelEvent` | 1.3 | Mob HP/DMG boost, zombie speed boost, husk replace, silent creeper flag, headless creeper, XP orb age, golem player-created reset |
| `LivingEntityUseItemEvent.Finish` | 1.3 | Raw food → Hunger + damage, golden apple effect strip |
| `LivingEquipmentChangeEvent` | 2.0 | Revert forbidden armor if it reaches a player armor slot |
| `LivingHurtEvent` | 1.6 | Fall ×1.5, zombie grab, skeleton arrow ×1.5, lava ×1.5, cactus ×2, sweet berries ×3, zombie infection |
| `PlayerInteractEvent.RightClickItem` | 2.0 | Cancel right-click equip for forbidden armor |
| `PlayerSleepInBedEvent` | 1.4, 2.1 | 20% sleep fail; dark-night sleep block |
| `PlayerWakeUpEvent` | 1.4, 1.13 | Phantom-bump after sleep, awake-tick reset |
| `PlayerInteractEvent.EntityInteract` | 1.7 | Cancel villager trade |
| `PlayerEvent.PlayerRespawnEvent` | 1.13 | Death fever, awake-tick reset |
| `PlayerEvent.Clone` | 1.14 | 50% XP keep on death |
| `PlayerEvent.BreakSpeed` | 1.1 | Cancel log break with non-axe tool |
| `ViewportEvent.ComputeFogColor` / `RenderFog` | 2.1 | Client-only dark-night fog |
| `RegisterDimensionSpecialEffectsEvent` | 2.1 | Client-only overworld lightmap adjustment |

---

## Patterns & conventions

### 1. All numeric parameters are named constants

Every magic number lives as a `private static final` at the top of the file
that uses it. Searchable, explanatory comments. Example:

```java
// Encumbrance: ≥ this many filled inventory slots → Slowness I + Mining Fatigue I.
private static final int    ENCUMBRANCE_FILL_THRESHOLD = 10;
private static final int    ENCUMBRANCE_REFRESH_INTERVAL_TICKS = 40;
private static final int    ENCUMBRANCE_EFFECT_DURATION_TICKS = 60;
```

### 2. Permanent attribute modifiers use stable UUIDs

For "set X once and forget it" buffs (HP cap, mob HP boost), we use
`AttributeModifier` with a known UUID and `Operation.ADDITION` /
`MULTIPLY_TOTAL`. Idempotency: before adding, remove any existing modifier
with the same UUID, so re-applying on respawn / dimension change doesn't
stack. Example:

```java
private static final UUID PLAYER_HP_CAP_UUID =
        UUID.fromString("4e3cce71-5872-4f6d-bb29-f31ed6c9fa03");

AttributeInstance attr = player.getAttribute(Attributes.MAX_HEALTH);
if (attr.getModifier(PLAYER_HP_CAP_UUID) == null) {
    attr.addPermanentModifier(new AttributeModifier(
            PLAYER_HP_CAP_UUID, "GTW player HP cap", -10.0,
            AttributeModifier.Operation.ADDITION));
}
```

UUIDs in this codebase follow the pattern `4e3cce71-5872-4f6d-bb29-f31ed6c9faNN`
where `NN` is incremented for each modifier. Keeps them visually distinct.

### 3. Dynamic (transient) modifiers via toggleSpeedModifier

For "active while X is true" buffs (sneak slow, snow slow), we use a helper
that adds the modifier when the condition is true and removes it when false.

### 4. Vanilla recipe disabling uses datapack overrides

To disable vanilla recipes, add a same-path JSON under
`src/main/resources/data/minecraft/recipes/` with `forge:conditions` containing
`forge:false`. Forge skips the recipe during load. Phase 2.0 uses this for
iron/gold/diamond armor recipes.

### 5. Equip gates need both pre-check and rollback

Right-click armor equip is cancellable via `PlayerInteractEvent.RightClickItem`.
Inventory clicks and other equip paths are safer to catch with
`LivingEquipmentChangeEvent`, which is not cancellable; restore the old slot
item and return/drop the forbidden item instead.
The modifier is `Operation.MULTIPLY_TOTAL`, transient (not permanent), so it
doesn't persist across reloads. See `HardcoreEvents.toggleSpeedModifier`.

### 6. Dark nights are calendar-based, not sleep-count-based

Phase 2.1 uses `level.getDayTime() / 24000 + 1` to decide whether the current
night is day 5/10/15/etc. Vanilla sleep advances `dayTime`, so skipped nights
still count correctly. Keep dark-night schedule logic in
`DarkNightEvents.isDarkNight`.

### 7. Client visuals must stay client-only

`DarkNightClientEvents` is guarded with `@Mod.EventBusSubscriber(... Dist.CLIENT)`.
Do not reference `net.minecraft.client.*` classes from common event handlers or
dedicated servers will crash during class loading.

### 8. Per-player persistent flags via getPersistentData

For state that needs to survive logout (sleep deprivation counter), we write
to `player.getPersistentData()` (a `CompoundTag` Forge gives every entity).
Tag names are prefixed `GTW...`:

```java
private static final String AWAKE_TICKS_TAG = "GTWAwakeTicks";
long awake = player.getPersistentData().getLong(AWAKE_TICKS_TAG);
player.getPersistentData().putLong(AWAKE_TICKS_TAG, awake + 1L);
```

For per-entity boolean flags (silent creeper, headless creeper), same
mechanism on the `Mob` instance.

### 5. Reflection for private vanilla fields

Two cases need access to private fields:
- `Creeper.explosionRadius` — for headless creepers.
- `ExperienceOrb.age` — for shortened XP orb lifetime.

Pattern: lazy reflective field lookup in a `static {}` block, with try/catch
producing a `null` if the field is missing. At call sites, `null`-check before
use:

```java
private static final Field XP_ORB_AGE;
static {
    Field f;
    try { f = ExperienceOrb.class.getDeclaredField("age"); f.setAccessible(true); }
    catch (NoSuchFieldException e) { f = null; }
    XP_ORB_AGE = f;
}
```

Forge MDK 1.20.1 uses **Mojang mappings** out of the box, so field names like
`age` and `explosionRadius` are the same in the dev environment and in
production (the runtime pulls Mojang mappings via the Forge classloader).

### 6. Server-side gating

Every event handler that touches gameplay should check `level().isClientSide`
and return early on the client. Client/server desync is the #1 source of
weirdness in Forge mods.

```java
if (player.level().isClientSide) return;
if (player.isCreative() || player.isSpectator()) return;
```

### 7. Damage source choice for HUD feedback

When dealing periodic environmental damage, choose a source whose vanilla
HUD icon matches the cause:
- Heat → `damageSources().inFire()` (fire icon, "X went up in flames")
- Cold → `damageSources().freeze()` (snowflake icon)
- Crushing/generic → `damageSources().generic()`

This gives the player a visible cue that the damage is intentional, not a bug.

### 8. Idempotent capability access in PlayerEvent.Clone

When the player dies and respawns, the `Clone` event gives you both the old
(dead) Player instance and the new one. The old one has its capabilities
invalidated by vanilla. To safely read fields:

```java
Player old = event.getOriginal();
old.reviveCaps();          // re-enable read of caps + fields
try {
    int xp = old.totalExperience;
    // ... copy / partial restore ...
} finally {
    old.invalidateCaps();  // ALWAYS pair with revive
}
```

---

## Gotchas

### tickCount % N == 0 firing schedule

`event.player.tickCount` is the count of ticks since this player joined the
current dimension. It's NOT a global counter. So `tickCount % N == 0` only
fires at multiples of N **after the player joined**. If the player just
teleported, you may wait up to N ticks for the first hit. Don't assume it
fires at world-time-0.

### Vanilla regen is gated on foodLevel ≥ 18

Phase 1.4 broke vanilla regen by capping `foodLevel = 6` (v1). v2 lets food
naturally fall instead, so vanilla regen kicks in only after the player eats
heavily. **Periodic environmental damage (rain, cold, heat) needs to be large
enough or fast enough to outpace this regen** when the player has eaten —
otherwise it's invisible.

After Phase 1.4 v2, foodLevel hovers around 6. Vanilla regen is OFF in that
state. Damage of `0.5 HP / 30 sec` is visible. After eating cooked food the
player's foodLevel jumps to 14–20 → vanilla regen fires (1 HP / 4 sec). Now
small periodic damage gets healed faster than dealt.

The post-1.14 fix bumped desert heat and snow-biome cold to 2 HP per interval
to ensure visibility regardless of food state. The older Phase 1.6 rain and
cold-night drips stayed at 1 HP per interval.

### LivingHurtEvent fires before armor reduction

Multiplying damage in `LivingHurtEvent` multiplies the *raw* damage. Vanilla
then applies armor reduction. Effectively means:
- For unarmored mobs: `event.setAmount(amount * 2.0)` doubles received damage.
- For armored mobs: doubles base damage, but armor still reduces afterward.

Use `LivingHurtEvent` for "armor still helps" multipliers, and
`LivingDamageEvent` for "true damage" multipliers (post-armor). Skeleton
arrows (Phase 1.6) use `LivingHurtEvent`, so 1.5× pierces through armor's
reduction proportionally — feels armor-piercing but isn't true.

### EntityJoinLevelEvent fires for chunk loads, not just spawns

When a chunk loads, all entities in it fire `EntityJoinLevelEvent`. So our
husk-replace-zombie hook would replace zombies on every chunk load too,
duplicating mobs over time. Mitigation: persist a marker tag
(`GTWReplacedToHusk`) on the new husk so we don't re-process. Currently the
risk is limited because we only check `entity.getClass() == Zombie.class`
and the replacement is already a Husk, but if vanilla ever adds a Zombie
subclass we don't recognize, this could still process.

Better long-term: gate on `MobSpawnType.NATURAL` only via a wrapper around
the vanilla spawn pathway. Phase 2 cleanup target.

### Player.causeFoodExhaustion is server-only

Calling `player.causeFoodExhaustion(0.05F)` works only on `ServerPlayer`. The
generic `Player` class has the method but it's a no-op on client. We always
gate on `!isClientSide` so this is fine, but worth knowing.

### LivingTickEvent fires extremely often

Every entity, every tick. Cheap stuff only. If you need to do work per entity
per second, use `if (entity.tickCount % 20 != 0) return;` early.

---

## How to add a new mechanic (template)

1. Decide where: per-player tick? per-entity tick? per-hit? per-spawn? Match
   to the Forge event in the table above.
2. Add a constant block at the top of the matching file describing the
   parameter:
   ```java
   // My new thing: damage X every Y sec when Z is true.
   private static final int    MY_INTERVAL_TICKS = 200;
   private static final float  MY_DAMAGE = 1.0F;
   ```
3. Add a method (or extend an existing one) annotated `@SubscribeEvent`:
   ```java
   @SubscribeEvent
   public void onWhatever(WhateverEvent event) {
       if (event.player.level().isClientSide) return;
       if (event.player.tickCount % MY_INTERVAL_TICKS != 0) return;
       // ... condition checks ...
       event.player.hurt(damageSources().generic(), MY_DAMAGE);
   }
   ```
4. Test with `./gradlew compileJava` first (fast). Then `./gradlew build`.
5. Drop the .jar into a Forge mods folder, launch a survival world, verify.

If the mechanic needs new items, blocks, or recipes, add them via:
- `ModItems.java` (deferred register an Item).
- `assets/glebthanwolves/models/item/<id>.json` (model).
- `assets/glebthanwolves/lang/{en_us,ru_ru}.json` (display name).
- `data/glebthanwolves/recipes/<id>.json` (recipe).
- (optional) `data/glebthanwolves/loot_modifiers/<id>.json` (drop rule).

---

## Where to find things quickly

| Question | Answer |
|---|---|
| Where's the player tick handler? | `HardcoreEvents.onPlayerTick` |
| Where's the level tick handler? | `WorldEvents.onLevelTick` |
| Where's the mob-join handler? | `HardcoreEvents.onEntityJoinLevel` |
| Where's the hurt handler? | `HardcoreEvents.onLivingHurt` |
| Where are item drops modified? | `data/glebthanwolves/loot_modifiers/*.json` |
| Where are recipes defined? | `data/glebthanwolves/recipes/*.json` |
| Where are item names translated? | `assets/glebthanwolves/lang/*.json` |
| Where's the mod registered with Forge? | `GlebThanWolves.java` constructor |
