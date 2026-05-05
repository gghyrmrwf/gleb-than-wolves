package com.gghyrmrwf.glebthanwolves.events;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

/**
 * Phase 1.3 — world-level hardcore tweaks.
 *
 * — Daytime advances at 1.5× speed (every other server tick we add +1 to the day
 *   time). Night progresses at vanilla speed. Net effect: night arrives ~1.5×
 *   faster than vanilla.
 * — At night we trigger one extra hostile-mob spawn attempt every 60 ticks per
 *   online player with 50% probability. The mob is placed at a random surface
 *   block 24–48 blocks from the player. Standard spawn rules (light, terrain,
 *   collision) still apply, so failed attempts produce nothing.
 *
 * Both behaviours respect the `doDaylightCycle` and `doMobSpawning` game rules.
 */
public class WorldEvents {

    private static final List<EntityType<? extends Mob>> NIGHT_HOSTILES = List.of(
            EntityType.ZOMBIE,
            EntityType.SKELETON,
            EntityType.SPIDER,
            EntityType.CREEPER
    );

    private static final int EXTRA_SPAWN_INTERVAL_TICKS = 60;
    private static final float EXTRA_SPAWN_CHANCE = 0.5F;

    private static final int MIN_SPAWN_DISTANCE = 24;
    private static final int MAX_SPAWN_DISTANCE = 48;

    // Meteors at night (Phase 1.12).
    private static final int    METEOR_CHECK_INTERVAL_TICKS = 200;
    private static final float  METEOR_PER_CHECK_CHANCE = 0.02F;
    private static final int    METEOR_HORIZONTAL_OFFSET = 50;
    private static final int    METEOR_HORIZONTAL_RANGE  = 60; // 50..110 blocks away
    private static final double METEOR_SPAWN_HEIGHT = 200.0D;
    private static final int    METEOR_EXPLOSION_POWER = 3;

    // Witches spawn anywhere, not just huts (Phase 1.11 #10).
    // Per-player every 30 minutes (36000 ticks), 50% chance to attempt a witch spawn
    // 30..64 blocks away on a valid surface block (any biome, any time of day).
    private static final int   WITCH_CHECK_INTERVAL_TICKS = 36000;
    private static final float WITCH_SPAWN_CHANCE = 0.50F;
    private static final int   WITCH_MIN_DISTANCE = 30;
    private static final int   WITCH_MAX_DISTANCE = 64;

    // Cave ambushes (Phase 1.15): rare monster pressure in deep, unlit caves.
    private static final List<EntityType<? extends Mob>> CAVE_AMBUSH_MOBS = List.of(
            EntityType.ZOMBIE,
            EntityType.SKELETON,
            EntityType.SPIDER
    );
    private static final int   CAVE_AMBUSH_CHECK_INTERVAL_TICKS = 600;
    private static final float CAVE_AMBUSH_CHANCE = 0.20F;
    private static final int   CAVE_AMBUSH_MAX_Y = 40;
    private static final int   CAVE_AMBUSH_MIN_DISTANCE = 8;
    private static final int   CAVE_AMBUSH_MAX_DISTANCE = 18;
    private static final int   CAVE_AMBUSH_TRIES = 12;

    @SubscribeEvent
    public void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (!(event.level instanceof ServerLevel level)) {
            return;
        }
        if (level.dimension() != Level.OVERWORLD) {
            return;
        }
        long timeOfDay = level.getDayTime() % 24000L;
        boolean isDay = timeOfDay < 12000L;

        // 1. Daytime ×1.5 — add an extra +1 to dayTime every other tick.
        if (isDay
                && level.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)
                && (level.getGameTime() & 1L) == 0L) {
            level.setDayTime(level.getDayTime() + 1L);
        }

        // 2. Extra hostile spawns at night.
        if (!isDay
                && level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)
                && level.getGameTime() % EXTRA_SPAWN_INTERVAL_TICKS == 0L) {
            for (ServerPlayer player : level.players()) {
                if (level.random.nextFloat() < EXTRA_SPAWN_CHANCE) {
                    trySpawnExtraMonster(level, player);
                }
            }
        }

        // 3. Meteors fall at night (Phase 1.12). Each player has a small chance
        //    every 10 sec to trigger a meteor that spawns 50–110 blocks away,
        //    falls from y=200 straight down, and explodes on impact.
        if (!isDay && level.getGameTime() % METEOR_CHECK_INTERVAL_TICKS == 0L) {
            for (ServerPlayer player : level.players()) {
                if (level.random.nextFloat() < METEOR_PER_CHECK_CHANCE) {
                    spawnMeteorNear(level, player);
                }
            }
        }

        // 4. Witches spawn anywhere, every 30 min per player (Phase 1.11 #10).
        if (level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)
                && level.getGameTime() % WITCH_CHECK_INTERVAL_TICKS == 0L
                && level.getGameTime() > 0L) {
            for (ServerPlayer player : level.players()) {
                if (level.random.nextFloat() < WITCH_SPAWN_CHANCE) {
                    trySpawnWitch(level, player);
                }
            }
        }

        if (level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)
                && level.getGameTime() % CAVE_AMBUSH_CHECK_INTERVAL_TICKS == 0L) {
            for (ServerPlayer player : level.players()) {
                if (isPlayerInDeepDarkCave(level, player)
                        && level.random.nextFloat() < CAVE_AMBUSH_CHANCE) {
                    trySpawnCaveAmbush(level, player);
                }
            }
        }
    }

    private static boolean isPlayerInDeepDarkCave(ServerLevel level, ServerPlayer player) {
        BlockPos pos = player.blockPosition();
        return pos.getY() <= CAVE_AMBUSH_MAX_Y
                && !level.canSeeSky(pos)
                && level.getBrightness(LightLayer.BLOCK, pos) <= 7;
    }

    private static void trySpawnWitch(ServerLevel level, ServerPlayer player) {
        RandomSource rand = level.random;
        double angle = rand.nextDouble() * Math.PI * 2D;
        int distance = WITCH_MIN_DISTANCE + rand.nextInt(WITCH_MAX_DISTANCE - WITCH_MIN_DISTANCE + 1);
        int x = player.blockPosition().getX() + (int) Math.round(Math.cos(angle) * distance);
        int z = player.blockPosition().getZ() + (int) Math.round(Math.sin(angle) * distance);

        BlockPos columnPos = new BlockPos(x, 0, z);
        if (!level.hasChunkAt(columnPos)) {
            return;
        }

        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        BlockPos spawnPos = new BlockPos(x, y, z);

        if (!level.getBlockState(spawnPos).isAir()
                || !level.getBlockState(spawnPos.above()).isAir()) {
            return;
        }
        BlockPos belowPos = spawnPos.below();
        if (!level.getBlockState(belowPos).isFaceSturdy(level, belowPos, Direction.UP)) {
            return;
        }

        Mob witch = EntityType.WITCH.create(level);
        if (witch == null) {
            return;
        }
        witch.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D,
                rand.nextFloat() * 360F, 0F);
        witch.finalizeSpawn(level, level.getCurrentDifficultyAt(spawnPos),
                MobSpawnType.NATURAL, null, null);
        level.addFreshEntity(witch);
    }

    private static void spawnMeteorNear(ServerLevel level, ServerPlayer player) {
        RandomSource rand = level.random;
        double angle = rand.nextDouble() * Math.PI * 2D;
        int distance = METEOR_HORIZONTAL_OFFSET + rand.nextInt(METEOR_HORIZONTAL_RANGE);
        double sx = player.getX() + Math.cos(angle) * distance;
        double sz = player.getZ() + Math.sin(angle) * distance;
        double sy = METEOR_SPAWN_HEIGHT;

        // Aim straight down with a slight horizontal nudge for visual variety.
        double tdx = (rand.nextDouble() - 0.5D) * 0.2D;
        double tdy = -1.0D;
        double tdz = (rand.nextDouble() - 0.5D) * 0.2D;

        LargeFireball meteor = new LargeFireball(level, player, tdx, tdy, tdz, METEOR_EXPLOSION_POWER);
        meteor.setPos(sx, sy, sz);
        level.addFreshEntity(meteor);
    }

    private static void trySpawnExtraMonster(ServerLevel level, ServerPlayer player) {
        RandomSource rand = level.random;

        double angle = rand.nextDouble() * Math.PI * 2D;
        int distance = MIN_SPAWN_DISTANCE + rand.nextInt(MAX_SPAWN_DISTANCE - MIN_SPAWN_DISTANCE + 1);
        int x = player.blockPosition().getX() + (int) Math.round(Math.cos(angle) * distance);
        int z = player.blockPosition().getZ() + (int) Math.round(Math.sin(angle) * distance);

        BlockPos columnPos = new BlockPos(x, 0, z);
        if (!level.hasChunkAt(columnPos)) {
            return;
        }

        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        BlockPos spawnPos = new BlockPos(x, y, z);

        if (!level.getBlockState(spawnPos).isAir()
                || !level.getBlockState(spawnPos.above()).isAir()) {
            return;
        }
        BlockPos belowPos = spawnPos.below();
        if (!level.getBlockState(belowPos).isFaceSturdy(level, belowPos, Direction.UP)) {
            return;
        }
        if (level.getBrightness(LightLayer.BLOCK, spawnPos) > 7) {
            return;
        }

        EntityType<? extends Mob> type = NIGHT_HOSTILES.get(rand.nextInt(NIGHT_HOSTILES.size()));
        Mob mob = type.create(level);
        if (mob == null) {
            return;
        }
        mob.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D,
                rand.nextFloat() * 360F, 0F);

        if (!mob.checkSpawnRules(level, MobSpawnType.NATURAL) || !mob.checkSpawnObstruction(level)) {
            mob.discard();
            return;
        }

        mob.finalizeSpawn(level, level.getCurrentDifficultyAt(spawnPos),
                MobSpawnType.NATURAL, null, null);
        level.addFreshEntity(mob);
    }

    private static void trySpawnCaveAmbush(ServerLevel level, ServerPlayer player) {
        RandomSource rand = level.random;
        BlockPos origin = player.blockPosition();

        for (int i = 0; i < CAVE_AMBUSH_TRIES; i++) {
            double angle = rand.nextDouble() * Math.PI * 2D;
            int distance = CAVE_AMBUSH_MIN_DISTANCE
                    + rand.nextInt(CAVE_AMBUSH_MAX_DISTANCE - CAVE_AMBUSH_MIN_DISTANCE + 1);
            int x = origin.getX() + (int) Math.round(Math.cos(angle) * distance);
            int y = origin.getY() + rand.nextInt(9) - 4;
            int z = origin.getZ() + (int) Math.round(Math.sin(angle) * distance);
            BlockPos spawnPos = new BlockPos(x, y, z);

            if (!level.hasChunkAt(spawnPos)
                    || level.canSeeSky(spawnPos)
                    || level.getBrightness(LightLayer.BLOCK, spawnPos) > 7
                    || !level.getBlockState(spawnPos).isAir()
                    || !level.getBlockState(spawnPos.above()).isAir()) {
                continue;
            }
            BlockPos belowPos = spawnPos.below();
            if (!level.getBlockState(belowPos).isFaceSturdy(level, belowPos, Direction.UP)) {
                continue;
            }

            EntityType<? extends Mob> type = CAVE_AMBUSH_MOBS.get(rand.nextInt(CAVE_AMBUSH_MOBS.size()));
            Mob mob = type.create(level);
            if (mob == null) {
                return;
            }
            mob.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D,
                    rand.nextFloat() * 360F, 0F);
            if (!mob.checkSpawnRules(level, MobSpawnType.NATURAL) || !mob.checkSpawnObstruction(level)) {
                mob.discard();
                continue;
            }
            mob.finalizeSpawn(level, level.getCurrentDifficultyAt(spawnPos),
                    MobSpawnType.NATURAL, null, null);
            level.addFreshEntity(mob);
            return;
        }
    }
}
